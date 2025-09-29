FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy source code
COPY src ./src

# Fetch dependencies (Gson). Add more jars to libs/ if needed
RUN mkdir -p libs out \
	&& curl -L -o libs/gson.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar \
	&& find src -name "*.java" > sources.list \
	&& javac -cp libs/gson.jar -d out @sources.list

# Optional: create a thin runtime image that can run the bot
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

COPY --from=build /app/out ./out
COPY --from=build /app/libs ./libs

# Default command only runs the program if desired; override as needed
CMD ["java", "-cp", "out:libs/gson.jar", "com.bomberman.Main", "localhost", "8888", "SmartBot"]

