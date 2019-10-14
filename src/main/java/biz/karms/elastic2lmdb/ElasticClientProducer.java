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

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

import static biz.karms.elastic2lmdb.Config.*;

/**
 * @author Michal Karm Babacek
 */
@ApplicationScoped
public class ElasticClientProducer {

    @Inject
    private transient Logger log;

    private Client client;
    private Node node;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        if (client == null) {
            if (node == null) {
                log.log(Level.INFO, "Elastic client node doesn't exists, creating new one");
                node = NodeBuilder.nodeBuilder()
                        .settings(ImmutableSettings.settingsBuilder()
                                .put("http.enabled", false)
                                .put("discovery.zen.ping.multicast.enabled", true)
                                .put("discovery.zen.ping.timeout", "3s")
                                //.put("network.host", "_" + E2L_NIC + ":ipv4_")
                                .putArray("discovery.zen.ping.unicast.hosts", E2L_ELASTIC_HOST + ":" + E2L_ELASTIC_PORT)
                                .put("cluster.name", E2L_ELASTIC_CLUSTER)
                                .put("discovery.zen.minimum_master_nodes", 1)
                        )
                        .client(true)
                        .data(false)
                        .node();
            }
            client = node.client();
        }
        log.log(Level.INFO, "Elastic client created");
    }

    @Produces
    public Client produceClient() {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null, check init");
        }
        return client;
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        if (client != null) {
            log.log(Level.INFO, "Closing elastic client");
            client.close();
        }
        if (node != null) {
            log.log(Level.INFO, "Closing elastic client node");
            node.close();
        }
    }
}
