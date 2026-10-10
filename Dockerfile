# ==========================================
# 1. BUILD: compila el .jar con Maven
# ==========================================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# Primero solo el pom: así las dependencias quedan cacheadas mientras no cambie
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests package \
    && cp target/*.jar app.jar

# ==========================================
# 2. RUNTIME: solo el JRE y el .jar
# ==========================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# 🛡️ No correr como root dentro del contenedor
RUN useradd --system --uid 1001 muebleria
USER muebleria

COPY --from=build /build/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
