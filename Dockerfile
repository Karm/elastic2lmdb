FROM centos:7
LABEL Author="Michal Karm Babacek <karm@email.cz>"
ENV JAVA_VERSION 1.8.0
RUN yum install java-${JAVA_VERSION}-openjdk-headless -y && yum clean all && rm -rf /var/cache/yum /tmp/* && \
    useradd -s /sbin/nologin elastic2lmdb
USER elastic2lmdb
WORKDIR /opt/
COPY --chown=elastic2lmdb:elastic2lmdb target/elastic2lmdb.jar start.sh /opt/
RUN if [[ ${ATACH_DEBUGGER:-False} == "True" ]]; then \
        export DBG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${E2L_DBG_PORT:-1661} -Xnoagent -Djava.compiler=NONE"; \
    fi; \
    echo 'handlers = java.util.logging.ConsoleHandler' > /opt/logging.properties && \
    echo "${E2L_LOGGING:-.level=ALL}" >> /opt/logging.properties && \
    echo 'export JAVA_OPTS="\
 -server \
 -Xms${E2L_MS_RAM:-1g} \
 -Xmx${E2L_MX_RAM:-1g} \
 -XX:MetaspaceSize=${E2L_METASPACE_SIZE:-128m} \
 -XX:MaxMetaspaceSize=${E2L_MAX_METASPACE_SIZE:-512m} \
 -XX:+UseG1GC \
 -XX:MaxGCPauseMillis=${E2L_MAX_GC_PAUSE_MILLIS:-2000} \
 -XX:InitiatingHeapOccupancyPercent=${E2L_INITIAL_HEAP_OCCUPANCY_PERCENT:-75} \
 -XX:+HeapDumpOnOutOfMemoryError \
 -XX:HeapDumpPath=/opt/ \
"' >> /opt/start.sh && \
   echo '\
 java \
 ${JAVA_OPTS} \
 -Djava.util.logging.config.file="/opt/logging.properties" \
 -DE2L_HOTROD_HOST=${E2L_HOTROD_HOST:-192.168.122.156} \
 -DE2L_HOTROD_PORT=${E2L_HOTROD_PORT:-11322} \
 -DE2L_HOTROD_CONN_TIMEOUT_S=${E2L_HOTROD_CONN_TIMEOUT_S:-60} \
 -DE2L_RESOLVER_CACHE_BATCH_SIZE_S=${E2L_RESOLVER_CACHE_BATCH_SIZE_S:-20} \
 -DE2L_RESOLVER_CACHE_GENERATOR_INTERVAL_S=${E2L_RESOLVER_CACHE_GENERATOR_INTERVAL_S:-0} \
 -DE2L_RESOLVER_CACHE_LISTENER_GENERATOR_INTERVAL_S=${E2L_RESOLVER_CACHE_LISTENER_GENERATOR_INTERVAL_S:-0} \
 -DE2L_RESOLVER_THREAT_TASK_RECORD_BATCH_SIZE_S=${E2L_RESOLVER_THREAT_TASK_RECORD_BATCH_SIZE_S:-1} \
 -DE2L_S3_ENDPOINT=${E2L_S3_ENDPOINT:-https://localhost:9000} \
 -DE2L_S3_ACCESS_KEY=${E2L_S3_ACCESS_KEY:-3chars} \
 -DE2L_S3_SECRET_KEY=${E2L_S3_SECRET_KEY:-8chars} \
 -DE2L_S3_BUCKET_NAME=${E2L_S3_BUCKET_NAME:-serve-file} \
 -DE2L_S3_REGION=${E2L_S3_REGION:-eu-west-1} \
 -DE2L_ENABLE_CACHE_LISTENERS=${E2L_ENABLE_CACHE_LISTENERS:-False} \
 -DE2L_REVERSE_RESOLVERS_ORDER=${E2L_REVERSE_RESOLVERS_ORDER:-False} \
 -DE2L_NOTIFICATION_ENDPOINT_TEMPLATE=${E2L_NOTIFICATION_ENDPOINT_TEMPLATE:-http://wsproxy_adddress:8080/wsproxy/rest/message/%d/updatecache} \
 -DE2L_NOTIFICATION_ENDPOINT_METHOD=${E2L_NOTIFICATION_ENDPOINT_METHOD:-POST} \
 -DE2L_NOTIFICATION_ENDPOINT_TIMEOUT_MS=${E2L_NOTIFICATION_ENDPOINT_TIMEOUT_MS:-2000} \
 -DE2L_USE_NOTIFICATION_ENDPOINT=${E2L_USE_NOTIFICATION_ENDPOINT:-False} \
 -DE2L_IOC_REFRESH_INTERVAL_S=${E2L_IOC_REFRESH_INTERVAL_S:-600} \
 ${DBG_OPTS} \
 -jar /opt/elastic2lmdb.jar \
 ' >> /opt/start.sh
CMD ["/opt/start.sh"]
