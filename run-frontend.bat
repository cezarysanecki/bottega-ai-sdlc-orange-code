@echo off
REM
REM Healthcare Demo - Frontend Dev Server
REM Starts Vite dev server with hot reload
REM Requires: Node.js 18+
REM

echo.
echo ==============================================
echo    Healthcare Demo - Frontend
echo ==============================================
echo.

REM Check for Node.js
where node >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Node.js is not installed.
    echo.
    echo Please install Node.js 18 or later from one of these sources:
    echo   - https://nodejs.org/ (recommended^)
    echo   - Using nvm-windows: https://github.com/coreybutler/nvm-windows
    echo.
    pause
    exit /b 1
)

REM Show Node.js version
for /f "tokens=*" %%g in ('node -v') do (
    echo Node.js version: %%g
)
echo.

REM Change to frontend directory
cd /d "%~dp0frontend\med-frontend"

REM Install dependencies if needed
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
    echo.
)

echo Starting frontend dev server...
echo UI will be available at: http://localhost:5173
echo.

REM Start Vite dev server
call npm run dev

echo.
echo Frontend stopped.
pause
