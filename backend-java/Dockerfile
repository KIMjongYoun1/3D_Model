FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# pom.xml과 소스 복사
COPY pom.xml .
COPY src ./src

# 빌드
RUN mvn clean package -DskipTests

# 실행 단계
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /app/target/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

