@echo off
setlocal
set KUBECONFIG="D:\Code\Projects\learning-project\kubeconfig-pa2.txt"

echo ========================================================
echo Deploying StudyHub to Kubernetes
echo ========================================================

@REM echo.
@REM echo [1/3] Applying Infrastructure (Database, Broker, etc.)
@REM kubectl apply -f ops/k8s
@REM if errorlevel 1 (
@REM     echo [ERROR] Failed to apply infrastructure manifests.
@REM     exit /b 1
@REM )

echo.
echo [2/3] Applying Secrets and ConfigMaps
kubectl apply -f ops/k8s/apps/configmap.yaml
kubectl apply -f ops/k8s/apps/secrets.yaml
if errorlevel 1 (
    echo [ERROR] Failed to apply config/secrets.
    exit /b 1
)

echo.
echo [3/3] Applying Application Services
kubectl apply -f ops/k8s/apps
if errorlevel 1 (
    echo [ERROR] Failed to apply application manifests.
    exit /b 1
)

echo.
echo ========================================================
echo Deployment requests sent to Kubernetes.
echo Check status with: kubectl get pods -n studyhub-dev
echo ========================================================
endlocal
exit /b 0
