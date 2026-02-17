@echo off
REM JWT RSA Key Generation Script for Local Development (Windows)
REM This script generates the RSA key pair needed for JWT token signing

setlocal enabledelayedexpansion

echo Creating certificates directory...
if not exist "src\main\resources\certs" mkdir "src\main\resources\certs"
cd "src\main\resources\certs"

echo.
echo Generating RSA key pair for JWT tokens...
echo.

REM Generate 2048-bit RSA private key
openssl genrsa -out private-key.pem 2048
if errorlevel 1 (
    echo.
    echo ERROR: Failed to generate private key. Make sure OpenSSL is installed and in PATH.
    echo.
    cd ..\..\..
    exit /b 1
)
echo [OK] Private key generated: private-key.pem
echo.

REM Extract public key from private key
openssl rsa -in private-key.pem -pubout -out public-key.pem
if errorlevel 1 (
    echo ERROR: Failed to extract public key.
    cd ..\..\..
    exit /b 1
)
echo [OK] Public key generated: public-key.pem
echo.

REM Display key information
echo ================================================================
echo RSA Key Generation Complete!
echo ================================================================
echo.
echo Keys have been created in: src\main\resources\certs\
echo.
echo Private Key:
openssl rsa -in private-key.pem -text -noout | findstr "Public-Key"
echo.
echo WARNING - SECURITY NOTES:
echo   * NEVER commit private-key.pem to version control
echo   * In production, store keys in AWS Secrets Manager
echo   * Restrict file access appropriately
echo   * See JWT_RSA_SETUP.md for detailed setup instructions
echo.
echo You can now run the application. It will load keys from:
echo   - Private: classpath:certs/private-key.pem
echo   - Public: classpath:certs/public-key.pem
echo.

cd ...\..\..
endlocal
