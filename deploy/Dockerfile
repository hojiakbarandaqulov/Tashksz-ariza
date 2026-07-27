FROM eclipse-temurin:17-jre-jammy

RUN groupadd --system --gid 10001 bot \
    && useradd --system --uid 10001 --gid bot --home-dir /app --shell /usr/sbin/nologin bot

WORKDIR /app
COPY --chown=bot:bot app.jar /app/app.jar

USER bot
ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-jar", "/app/app.jar"]
