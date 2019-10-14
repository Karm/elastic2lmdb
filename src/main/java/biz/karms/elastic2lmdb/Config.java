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

import java.util.Objects;

/**
 * @author Michal Karm Babacek
 */
public class Config {

    //public static final String E2L_NIC;
    public static final String E2L_ELASTIC_HOST;
    public static final String E2L_ELASTIC_PORT;
    public static final String E2L_ELASTIC_CLUSTER;
    public static final String E2L_ELASTIC_IOC_INDEX_ACTIVE;


    public static final int E2L_DEFAULT_SCHEDULER_POOL_SIZE;

    public static final int E2L_IOC_REFRESH_INTERVAL_S;

    static {
        //E2L_NIC = Objects.requireNonNull(System.getProperty("E2L_NIC");
        E2L_ELASTIC_HOST = Objects.requireNonNull(System.getProperty("E2L_ELASTIC_HOST"), "E2L_ELASTIC_HOST must be set");
        E2L_ELASTIC_PORT = Objects.requireNonNull(System.getProperty("E2L_ELASTIC_PORT"), "E2L_ELASTIC_PORT must be set");
        E2L_ELASTIC_CLUSTER = Objects.requireNonNull(System.getProperty("E2L_ELASTIC_CLUSTER"), "E2L_ELASTIC_CLUSTER must be set");
        E2L_ELASTIC_IOC_INDEX_ACTIVE = System.getProperty("E2L_ELASTIC_IOC_INDEX_ACTIVE", "iocs_v3");

        E2L_DEFAULT_SCHEDULER_POOL_SIZE = Integer.parseInt(System.getProperty("E2L_DEFAULT_SCHEDULER_POOL_SIZE", "10"));

        E2L_IOC_REFRESH_INTERVAL_S = Integer.parseInt(System.getProperty("E2L_IOC_REFRESH_INTERVAL_S", "1800"));
    }
}
