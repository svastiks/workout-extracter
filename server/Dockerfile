FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

RUN apk add --no-cache \
    bash \
    curl \
    python3 \
    py3-pip \
    findutils \
    grep \
    sed \
    wget \
    && pip3 install yt-dlp

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/workoutextract-1.0-SNAPSHOT.jar"] 