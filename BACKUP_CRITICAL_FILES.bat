@echo off
REM Backup Critical Files for FirstAid App
REM Run this script to create a backup package

echo ========================================
echo FirstAid App - Critical Files Backup
echo ========================================
echo.

REM Get current date
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set BACKUP_DATE=%datetime:~0,8%

REM Create backup folder on Desktop
set BACKUP_FOLDER=%USERPROFILE%\Desktop\FirstAidApp_BACKUP_%BACKUP_DATE%
echo Creating backup folder: %BACKUP_FOLDER%
mkdir "%BACKUP_FOLDER%" 2>nul

echo.
echo Copying critical files...
echo.

REM Copy keystore
if exist "firstaid-release.keystore" (
    copy "firstaid-release.keystore" "%BACKUP_FOLDER%\" >nul
    echo [OK] firstaid-release.keystore
) else (
    echo [MISSING] firstaid-release.keystore
)

REM Copy keystore properties
if exist "keystore.properties" (
    copy "keystore.properties" "%BACKUP_FOLDER%\" >nul
    echo [OK] keystore.properties
) else (
    echo [MISSING] keystore.properties
)

REM Copy local properties
if exist "local.properties" (
    copy "local.properties" "%BACKUP_FOLDER%\" >nul
    echo [OK] local.properties
) else (
    echo [MISSING] local.properties
)

REM Copy google services
if exist "app\google-services.json" (
    copy "app\google-services.json" "%BACKUP_FOLDER%\" >nul
    echo [OK] google-services.json
) else (
    echo [MISSING] google-services.json
)

REM Create a readme file
echo Critical Backup Files for FirstAid App > "%BACKUP_FOLDER%\README.txt"
echo Created: %date% %time% >> "%BACKUP_FOLDER%\README.txt"
echo. >> "%BACKUP_FOLDER%\README.txt"
echo IMPORTANT: >> "%BACKUP_FOLDER%\README.txt"
echo - Keep this folder safe and secure >> "%BACKUP_FOLDER%\README.txt"
echo - Upload to cloud storage (Google Drive, OneDrive) >> "%BACKUP_FOLDER%\README.txt"
echo - Copy to USB drive >> "%BACKUP_FOLDER%\README.txt"
echo - Keep passwords in password manager >> "%BACKUP_FOLDER%\README.txt"
echo. >> "%BACKUP_FOLDER%\README.txt"
echo If you lose firstaid-release.keystore, you CANNOT update your app! >> "%BACKUP_FOLDER%\README.txt"

echo.
echo ========================================
echo Backup Complete!
echo ========================================
echo.
echo Backup location: %BACKUP_FOLDER%
echo.
echo NEXT STEPS:
echo 1. Upload this folder to Google Drive
echo 2. Copy to USB drive
echo 3. Keep in safe location
echo.
echo Opening backup folder...
explorer "%BACKUP_FOLDER%"

pause

