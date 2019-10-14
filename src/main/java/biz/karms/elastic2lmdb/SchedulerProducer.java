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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static biz.karms.elastic2lmdb.Config.E2L_DEFAULT_SCHEDULER_POOL_SIZE;

/**
 * @author Michal Karm Babacek
 */
public class SchedulerProducer {

    @Produces
    @SchedulerPicker(SchedulerName.default_scheduler)
    public ScheduledExecutorService produceDefaultScheduler(InjectionPoint injectionPoint) {
        return Executors.newScheduledThreadPool(E2L_DEFAULT_SCHEDULER_POOL_SIZE);
    }
}
