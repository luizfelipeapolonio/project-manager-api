FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /application
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN apt-get update && apt-get install -y dos2unix && rm -rf /var/lib/apt/lists/*
RUN dos2unix ./mvnw
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests
RUN java -Djarmode=layertools -jar ./target/*.jar extract

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /application
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "org.springframework.boot.loader.launch.JarLauncher"]

EXPOSE 8080