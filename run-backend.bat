@echo off
REM
REM Healthcare Demo - Backend Server
REM Starts Spring Boot with embedded PostgreSQL
REM Requires: Java 22+
REM

echo.
echo ==============================================
echo    Healthcare Demo - Backend
echo ==============================================
echo.

REM Check for Java
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java is not installed.
    echo.
    echo Please install Java 22 or later from one of these sources:
    echo   - https://adoptium.net/ (recommended^)
    echo   - https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

REM Show Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    echo Java version: %%g
)
echo.

REM Change to backend directory
cd /d "%~dp0backend"

echo Starting backend server...
echo API will be available at: http://localhost:8080
echo.

REM Run with demo profile (embedded PostgreSQL)
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo -DskipTests

echo.
echo Backend stopped.
pause
