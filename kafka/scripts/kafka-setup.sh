
while ! /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list > /dev/null 2>&1
do
  sleep 2
done

echo "Kafka ready"  

/opt/kafka/bin/kafka-topics.sh \
  --create \
  --if-not-exists \
  --topic market \
  --bootstrap-server kafka:9092 \
  --partitions 1 \
  --replication-factor 1

echo "📄 Topics:"
/opt/kafka/bin/kafka-topics.sh \
  --list \
  --bootstrap-server kafka:9092