#!/bin/bash

# Port to kill
PORT=8080

echo "Checking for process on port $PORT..."

# Find and kill process using the port
PID=$(lsof -ti:$PORT)

if [ -z "$PID" ]; then
    echo "No process found on port $PORT"
else
    echo "Killing process $PID on port $PORT..."
    kill -9 $PID
    echo "Process killed successfully"
fi

echo "Starting Spring Boot application..."
./mvnw spring-boot:run
