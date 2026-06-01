# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom dependency layer to be cached
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat jar
COPY src ./src
RUN mvn clean package -DskipTests -q

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/aitracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]