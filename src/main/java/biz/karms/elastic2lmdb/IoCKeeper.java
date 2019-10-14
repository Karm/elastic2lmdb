/*
 *   Copyright 2019 contributors to elastic2lmdb
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package biz.karms.elastic2lmdb;

import biz.karms.crc64java.CRC64;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static biz.karms.elastic2lmdb.Config.E2L_ELASTIC_IOC_INDEX_ACTIVE;
import static biz.karms.elastic2lmdb.Config.E2L_IOC_REFRESH_INTERVAL_S;
import static java.nio.ByteBuffer.allocateDirect;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Michal Karm Babacek
 */
@ApplicationScoped
public class IoCKeeper {

    @Inject
    Logger log;

    @Inject
    @SchedulerPicker(SchedulerName.default_scheduler)
    ScheduledExecutorService scheduler;

    @Inject
    Client client;

    @Inject
    LMDB lmdb;

    final CRC64 hashf = CRC64.getInstance();

    final byte[] dummyFlag = new byte[]{1};

    private ScheduledFuture<?> future;

    public void foo() {

    }

    @PostConstruct
    public void init() {
        log.log(Level.INFO, "Thread " + Thread.currentThread().getName() + ": Creating one and only one IoCKeeper.");

        this.future = scheduler
                .scheduleAtFixedRate(
                        new Worker(),
                        0,
                        E2L_IOC_REFRESH_INTERVAL_S, SECONDS);
    }


    void insertIoC(final String fqdn) {

        // We want to store some data, so we will need a direct ByteBuffer.
        // Note that LMDB keys cannot exceed maxKeySize bytes (511 bytes by default).
        // Values can be larger.
        final ByteBuffer key = allocateDirect(17);
        final ByteBuffer val = allocateDirect(1);
        key.put(hashf.crc64BigInteger(fqdn.getBytes()).toByteArray()).flip();
        val.put(dummyFlag).flip();
        //final int valSize = val.remaining();

        // Now store it. Dbi.put() internally begins and commits a transaction (Txn).
        lmdb.getDb().put(key, val);






        /*
        // To fetch any data from LMDB we need a Txn. A Txn is very important in
        // LmdbJava because it offers ACID characteristics and internally holds a
        // read-only key buffer and read-only value buffer. These read-only buffers
        // are always the same two Java objects, but point to different LMDB-managed
        // memory as we use Dbi (and Cursor) methods. These read-only buffers remain
        // valid only until the Txn is released or the next Dbi or Cursor call. If
        // you need data afterwards, you should copy the bytes to your own buffer.
        try (Txn<ByteBuffer> txn = lmdb.env.txnRead()) {
            final ByteBuffer found = lmdb.db.get(txn, key);
            //  assertNotNull(found);

            // The fetchedVal is read-only and points to LMDB memory
            final ByteBuffer fetchedVal = txn.val();
            //assertThat(fetchedVal.remaining(), is(valSize));

            // Let's double-check the fetched value is correct
            //assertThat(UTF_8.decode(fetchedVal).toString(), is("Hello world"));
        }

        // We can also delete. The simplest way is to let Dbi allocate a new Txn...
        lmdb.db.delete(key);

        // Now if we try to fetch the deleted row, it won't be present
        try (Txn<ByteBuffer> txn = lmdb.env.txnRead()) {
            // assertNull(db.get(txn, key));
        }

*/
    }

    class Worker implements Runnable {
        @Override
        public void run() {
            log.log(Level.INFO, "Thread " + Thread.currentThread().getName() + ": Fetching IoCs...");

            final QueryBuilder qb = termQuery("active", true);

            SearchResponse scrollResp = client.prepareSearch(E2L_ELASTIC_IOC_INDEX_ACTIVE)
                    .setSearchType(SearchType.SCAN)
                    .setFetchSource(new String[]{"active", "accuracy", "source.fqdn"}, null)
                    .setScroll(new TimeValue(60000))
                    .setQuery(qb)
                    .setSize(100).execute().actionGet();

            int c = 0;
            while (true) {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    c++;
                    log.info(c + ", " + hit.getSource().toString());

                    //TODO Bullshit
                    insertIoC(hit.getSource().get("source").toString());

                }
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
                if (scrollResp.getHits().getHits().length == 0) {
                    log.log(Level.INFO, "Thread " + Thread.currentThread().getName() + ": No search hits...");

                    break;
                }
            }
        }
    }
}
