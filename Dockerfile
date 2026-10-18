# Stage 1: Build the JAR with Maven
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper and POM first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source code and build production jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create a non-root system group and user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# JVM ergonomics for containerized workloads
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
