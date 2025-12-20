# ETAPA 1: BUILD (Construcción)
# Usa una imagen oficial de Maven con JDK 21 para compilar el proyecto.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
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
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear un usuario no root por seguridad (mejor práctica profesional)
# Si hackean la app, no tienen permisos de root en el contenedor.
RUN addgroup -S messenger && adduser -S springuser -G messenger
USER springuser:messenger

# Copiar el .jar compilado desde la etapa de BUILD
COPY --from=build /app/target/*.jar app.jar

# Configurar variables de entorno por defecto (pueden sobreescribirse al ejecutar)
ENV SPRING_PROFILES_ACTIVE=prod

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
