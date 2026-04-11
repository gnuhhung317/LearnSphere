@echo off
setlocal

REM Usage: build_all_services.bat [registry] [tag]
REM Defaults: registry=gnuhhung317, tag=prod-<git-sha> (handled by inner scripts)

set "REGISTRY=%~1"
set "TAG=%~2"

echo ========================================================
echo Starting Build and Push for All Services
echo Registry: %REGISTRY%
echo Tag: %TAG%
echo ========================================================

echo.
echo ========================================================
echo [Step 1] Building Maven Projects (Clean Package)
echo ========================================================
pushd "services"
REM Use one of the sub-modules' maven wrapper to build the parent project
call auth-service\mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Maven build failed.
    popd
    exit /b 1
)
popd
echo.
echo ========================================================
echo [Step 2] Building Docker Images
echo ========================================================

set SERVICES=api-gateway auth-service user-service learning-service media-service search-service chat-service ai-service realtime-service

set "BASE_DIR=%~dp0"

for %%s in (%SERVICES%) do (
    echo.
    echo --------------------------------------------------------
    echo Building %%s ...
    echo --------------------------------------------------------
    pushd "%BASE_DIR%services\%%s"
    if exist "buid_and_push.bat" (
        call buid_and_push.bat "%REGISTRY%" "%TAG%"
    ) else (
        echo [ERROR] buid_and_push.bat not found in services\%%s
    )
    if errorlevel 1 (
        echo [ERROR] Failed to build %%s
        popd
        exit /b 1
    )
    popd
)

echo.
echo ========================================================
echo All services built and pushed successfully!
echo ========================================================
endlocal
exit /b 0
