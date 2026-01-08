@echo off
echo Starting StudyHub Core Services...

echo Starting Eureka Server...
start "Eureka Server" cmd /k "cd eureka-server && mvn spring-boot:run"

echo Waiting 15 seconds for Eureka to initialize...
timeout /t 15 /nobreak

echo Starting API Gateway...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"

echo Starting Auth Service...
start "Auth Service" cmd /k "cd auth-service && mvn spring-boot:run"

echo Starting User Service...
start "User Service" cmd /k "cd user-service && mvn spring-boot:run"

echo Starting Media Service...
start "Media Service" cmd /k "cd media-service && mvn spring-boot:run"

echoAll core services start command handlers initiated.
echo You can now run other services (Chat, AI, User, etc.) in your IDE for debugging.
pause
