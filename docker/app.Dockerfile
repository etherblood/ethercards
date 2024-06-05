FROM eclipse-temurin:17-jre-alpine
WORKDIR /home
COPY target/game-server-0.1.0.jar ./
COPY target/libs libs
COPY target/assets assets
RUN echo -n assets=assets/ > config.properties
ENTRYPOINT ["java", "-jar", "game-server-0.1.0.jar"]