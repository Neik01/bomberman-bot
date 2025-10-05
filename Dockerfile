FROM maven:3.9.4-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/bomberman-bot-1.0-SNAPSHOT.jar /app/bomberman-bot.jar

# Run the bot
CMD ["java", "-jar", "bomberman-bot.jar"]

