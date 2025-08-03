FROM amazoncorretto:17-alpine
WORKDIR /app

# Install required packages
RUN apk add --no-cache \
    bash \
    curl \
    python3 \
    py3-pip \
    findutils \
    grep \
    sed \
    && pip3 install --break-system-packages yt-dlp \
    && chmod +x $(which yt-dlp)

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/workoutextract-1.0-SNAPSHOT.jar"] 