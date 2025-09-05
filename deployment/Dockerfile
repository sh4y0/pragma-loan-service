FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp

# Crear usuario no root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el jar desde build/libs
COPY --chown=appuser:appgroup ../applications/app-service/build/libs/Loan-Service.jar app.jar

# Config JVM (opciones válidas para Temurin/HotSpot)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]