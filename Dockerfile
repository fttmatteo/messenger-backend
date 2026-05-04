# ETAPA 1: BUILD (Construcción)
# Usa una imagen oficial de Maven con JDK 17 para compilar el proyecto.
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar primero el pom.xml para aprovechar la caché de capas de Docker
# Si no cambias dependencias, Docker reusará esta capa sin descargar todo de nuevo.
COPY messenger/pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente y compilar
COPY messenger/src ./src
# -DskipTests para acelerar el build (tests corren en CI)
RUN mvn clean package -DskipTests

# ETAPA 2: RUN (Ejecución)
# Usa una imagen ligera (JRE) solo para correr la app, sin herramientas de compilación.
# Esto reduce el tamaño de la imagen final y mejora la seguridad.
FROM eclipse-temurin:17-jre
WORKDIR /app

# Crear un usuario no root y preparar directorios
RUN groupadd -r messenger && useradd -r -g messenger springuser \
    && mkdir -p /app/uploads && chown -R springuser:messenger /app
USER springuser:messenger

# Copiar el .jar compilado desde la etapa de BUILD
COPY --from=build /app/target/*.jar app.jar

# Configurar variables de entorno por defecto (pueden sobreescribirse al ejecutar)
ENV SPRING_PROFILES_ACTIVE=prod

# Forzar timezone de Colombia en el contenedor y JVM
ENV TZ=America/Bogota
ENV JAVA_OPTS="-Duser.timezone=America/Bogota -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando de inicio con timezone configurado
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
