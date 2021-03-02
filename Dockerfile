
##### Dependencies stage #####
FROM maven:3.6.3-amazoncorretto-11 AS dependencies
WORKDIR /usr/app
# Resolve dependencies and cache them
COPY pom.xml .
COPY payment-service/pom.xml payment-service/
COPY trustly-client/pom.xml trustly-client/
RUN mvn dependency:go-offline


##### Build stage #####
FROM dependencies AS build

# Copy application source and build it
COPY payment-service/src/main payment-service/src/main
COPY payment-service/lombok.config payment-service/
COPY trustly-client/src/main trustly-client/src/main
RUN mvn clean package


##### Test stage #####
FROM build AS test

# Copy test source and build+run tests
COPY payment-service/src/test payment-service/src/test
RUN mvn test


##### Assemble artifact #####
FROM amazoncorretto:11 AS assemble

# Fetch the datadog agent
RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'

# Copy the jar from build stage to this one
COPY --from=build /usr/app/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar .

# Define entry point
ENTRYPOINT java -javaagent:/dd-java-agent.jar -jar payment-service-0.0.1-SNAPSHOT.jar
