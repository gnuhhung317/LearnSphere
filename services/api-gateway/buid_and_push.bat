@echo off
setlocal

REM Usage: build_and_push.bat [registry] [tag]
REM Defaults: registry=gnuhhung317, tag=prod-<git-sha>

set "REGISTRY=%~1"
if "%REGISTRY%"=="" set "REGISTRY=gnuhhung317"

set "IMAGE_NAME=studyhub-api-gateway"
set "TAG=%~2"

if "%TAG%"=="" (
  for /f "delims=" %%i in ('git rev-parse --short HEAD 2^>nul') do set "GIT_SHA=%%i"
  if defined GIT_SHA (set "TAG=latest") else set "TAG=prod-latest"
)

echo Registry: %REGISTRY%
echo Image: %IMAGE_NAME%
echo Tag: %TAG%
echo.



REM Build Docker image (context = api-gateway directory)
pushd "%~dp0"
echo Building Docker image %REGISTRY%/%IMAGE_NAME%:%TAG% ...
docker build -t "%REGISTRY%/%IMAGE_NAME%:%TAG%" .
if errorlevel 1 (
  echo Docker build failed.
  popd
  exit /b 1
)

echo Pushing image to registry...
docker push "%REGISTRY%/%IMAGE_NAME%:%TAG%"
if errorlevel 1 (
  echo Docker push failed.
  popd
  exit /b 1
)

echo Done: %REGISTRY%/%IMAGE_NAME%:%TAG%
popd
endlocal
exit /b 0