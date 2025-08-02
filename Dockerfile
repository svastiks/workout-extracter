FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY server/mvnw .
COPY server/.mvn .mvn
COPY server/pom.xml .
COPY server/src src

RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/workoutextract-1.0-SNAPSHOT.jar"] 