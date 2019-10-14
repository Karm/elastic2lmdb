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

import org.lmdbjava.Dbi;
import org.lmdbjava.Env;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.Env.create;

/**
 * @author Michal Karm Babacek
 */
@ApplicationScoped
public class LMDB {

    @Inject
    Logger log;

     private Dbi<ByteBuffer> db;
     private Env<ByteBuffer> env;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        log.log(Level.INFO, "Thread " + Thread.currentThread().getName() + ": Creating LMDB.");
        // We need a storage directory first.
        // The path cannot be on a remote file system.
        final File path = new File("./LMDBDir");
        path.mkdir();
        final String DB_NAME = "IoCs";
        // We always need an Env. An Env owns a physical on-disk storage file. One
        // Env can store many different databases (ie sorted maps).
        this.env = create()
                // LMDB also needs to know how large our DB might be. Over-estimating is OK.
                .setMapSize(1_000_000)
                // LMDB also needs to know how many DBs (Dbi) we want to store in this Env.
                .setMaxDbs(1)
                // Now let's open the Env. The same path can be concurrently opened andgetDb
                // used in different processes, but do not open the same path twice in
                // the same process at the same time.
                .open(path);

        // We need a Dbi for each DB. A Dbi roughly equates to a sorted map. The
        // MDB_CREATE flag causes the DB to be created if it doesn't already exist.
        this.db = env.openDbi(DB_NAME, MDB_CREATE);
    }

    public Dbi<ByteBuffer> getDb() {
        return db;
    }

    public Env<ByteBuffer> getEnv() {
        return env;
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        if (env != null) {
            log.log(Level.INFO, "Closing env");
            env.close();
        }
    }
}
