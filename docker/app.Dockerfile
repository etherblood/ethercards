FROM eclipse-temurin:17-jre-alpine
WORKDIR /home
COPY target/game-server-0.1.0.jar config.properties ./
COPY target/libs libs
COPY target/assets assets
ENTRYPOINT ["java", "-jar", "game-server-0.1.0.jar"]