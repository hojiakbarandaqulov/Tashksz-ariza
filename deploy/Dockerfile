# 1-bosqich: loyiha kodidan bajariladigan Spring Boot JAR faylini yig'amiz.
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

# Gradle wrapper va build fayllarini avval nusxalash Docker keshidan samarali foydalanadi.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Ilova manba kodini nusxalab, production JAR faylini yaratamiz.
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# 2-bosqich: yakuniy image ichida faqat Java runtime va tayyor JAR qoladi.
FROM eclipse-temurin:17-jre-jammy

RUN groupadd --system --gid 10001 bot \
    && useradd --system --uid 10001 --gid bot --home-dir /app --shell /usr/sbin/nologin bot

WORKDIR /app
COPY --from=builder --chown=bot:bot /workspace/build/libs/*.jar /app/app.jar

USER bot
ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-jar", "/app/app.jar"]
