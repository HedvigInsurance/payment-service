FROM openjdk:8


ADD target/payment-service-0.0.1-SNAPSHOT.jar /

ENTRYPOINT java -XX:+UseSerialGC -jar payment-service-0.0.1-SNAPSHOT.jar
