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

package biz.karms;

import biz.karms.elastic2lmdb.Elastic2LMDB;
import biz.karms.elastic2lmdb.IoCKeeper;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Michal Karm Babacek
 */
@EnableAutoWeld
@AddPackages(Elastic2LMDB.class)
public class IoCKeeperTest {

    @Inject
    IoCKeeper ioCKeeper;

    @Inject
    Client client;

    @Test
    void lolTest() throws InterruptedException, IOException, URISyntaxException {


        System.setProperty("E2L_IOC_REFRESH_INTERVAL_S","5000");


        assertNotNull(ioCKeeper);

        ioCKeeper.foo();

        System.out.println("Waiting...");
        Thread.sleep(10000);
        System.out.println("Done Waiting.");

//      - run: curl -XPUT localhost:9200/_template/iocs -d @integration-tests/src/test/resources/elastic_iocs.json

        URI fileLocation = this.getClass().getClassLoader().getResource("elastic_iocs.json").toURI();

        URI dataLocation = this.getClass().getClassLoader().getResource("10-iocs.json").toURI();

        Path path = Paths.get(fileLocation);
        String templateString = java.nio.file.Files.lines(path).collect(Collectors.joining());

        client.admin().indices().putTemplate(
                new PutIndexTemplateRequest("iocs").source(templateString)
        );

        final AtomicBoolean running = new AtomicBoolean(true);
        Runnable spinner = () -> {
            final Random rng = new Random();
            while (running.get()) {
                System.out.print("\033[1K\033[1G");
                System.out.print("Loading IoCs... ");
                for (int i = 0; i < rng.nextInt(80); i++) System.out.print("▍");
                System.out.flush();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        new Thread(spinner).start();
        try (Stream<String> lines = Files.lines(Paths.get(dataLocation))) {
            lines.forEach(line -> {

                IndexResponse response = client.prepareIndex("iocs_v3", "intelmq")
                        .setSource(line)
                        .execute()
                        .actionGet();
/*
                String _index = response.getIndex();
// Type name
                String _type = response.getType();
// Document ID (generated or not)
                String _id = response.getId();
// Version (if it's the first time you index this document, you will get: 1)
                long _version = response.getVersion();
// isCreated() is true if the document is a new one, false if it has been updated
                boolean created = response.isCreated();

  */
            });


        }


        running.set(false);

        ioCKeeper.init();

        System.out.println("Waiting...");
        Thread.sleep(20000);
        System.out.println("Done Waiting.");



    }
}
