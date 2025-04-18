FROM maven:3.9.6-eclipse-temurin-21 AS developer
RUN apt update && apt install -y sudo
RUN useradd -m -u 1000 developer \
    && echo "developer ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers \
    && adduser developer sudo
RUN mkdir  /app /home/developer/.m2
RUN chown -R developer:developer /app /home/developer/.m2
USER developer

FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 3030
CMD ["java", "-jar", "app.jar"]
