#  Real-Time Data Pipeline (Kafka + Spark + Cassandra + Grafana)

##  Overview

This project implements a **real-time data streaming pipeline** using modern big data technologies. It ingests live market data, processes it in real time, stores it efficiently, and visualizes insights.

###  Tech Stack

* Apache Kafka – Data streaming platform
* Apache Spark – Real-time processing (Structured Streaming)
* Apache Cassandra – Scalable data storage
* Grafana – Data visualization
* Finnhub API – Live market data source
* Docker – Containerization

---

##  System Architecture

```text
Finnhub API
     ↓
Kafka Producer
     ↓
Kafka Topic (market)
     ↓
Spark Structured Streaming
     ↓
Cassandra (trades + aggregates)
     ↓
Grafana Dashboard
```

---

##  Project Structure

```text
.
├── docker-compose.yml
├── kafka/
├── cassandra/
├── StreamProcessor/
│   └── app.jar
├── FinnhubProducer/
└── grafana/
```











