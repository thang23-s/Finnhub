FROM apache/kafka:latest

USER root

COPY ./server.properties /opt/kafka/server.properties

COPY --chmod=755 ./scripts/kafka-setup.sh /kafka-setup.sh

CMD ["/bin/bash", "-c", "\
/opt/kafka/bin/kafka-storage.sh format -t $CLUSTER_ID -c /opt/kafka/server.properties || true && \
/opt/kafka/bin/kafka-server-start.sh /opt/kafka/server.properties & \
sleep 15 && \
/kafka-setup.sh && \
wait"]