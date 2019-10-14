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

import org.jboss.weld.environment.se.Weld;

import javax.enterprise.inject.spi.CDI;

/**
 * @author Michal Karm Babacek
 */
public class Elastic2LMDB {

    public static void main(String[] args) {
        Weld weld = new Weld().beanClasses(
                ElasticClientProducer.class,
                IoCKeeper.class,
                LoggerProducer.class,
                SchedulerProducer.class).disableDiscovery();
        weld.initialize();

        CDI.current().select(IoCKeeper.class).get().foo();

        /*
        Causes double init:
        Apr 12, 2019 3:08:45 PM biz.karms.elastic2lmdb.IoCKeeper init
        INFO: Thread main: Creating one and only one IoCKeeper.
        Apr 12, 2019 3:08:45 PM biz.karms.elastic2lmdb.IoCKeeper$Worker run
        INFO: Thread pool-1-thread-1: Fetching IoCs...
        Apr 12, 2019 3:08:45 PM biz.karms.elastic2lmdb.IoCKeeper init
        INFO: Thread main: Creating one and only one IoCKeeper.
        Apr 12, 2019 3:08:45 PM biz.karms.elastic2lmdb.IoCKeeper$Worker run
        INFO: Thread pool-1-thread-2: Fetching IoCs...
        */
    }
}
