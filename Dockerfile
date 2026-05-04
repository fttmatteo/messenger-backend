FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY messenger/pom.xml .
RUN mvn dependency:go-offline

COPY messenger/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r messenger && useradd -r -g messenger springuser \
    && mkdir -p /app/uploads && chown -R springuser:messenger /app

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

USER springuser:messenger

COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

ENV TZ=America/Bogota
ENV JAVA_OPTS="-Duser.timezone=America/Bogota -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
