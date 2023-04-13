#FROM openjdk:8-jdk-alpine
#FROM openjdk:11.0.16-slim
#FROM openjdk:8-jre-alpine
FROM openjdk:11.0.16-jre-slim
WORKDIR /app/
COPY target/minio-example.jar /app/minio-example.jar
ENTRYPOINT ["java","-jar","minio-example.jar"]
