
FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 AS sbt_builder

WORKDIR /sbt

COPY build.sbt .
COPY project ./project
COPY src ./src

RUN sbt clean assembly

FROM apache/spark:3.5.0

WORKDIR /opt/spark/app

COPY --from=sbt_builder /sbt/target/scala-2.12/streamprocessor-assembly-1.0.jar app.jar
