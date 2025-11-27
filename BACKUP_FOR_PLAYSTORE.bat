@echo off
echo ============================================
echo  FIRSTAID APP - CRITICAL FILES BACKUP
echo ============================================
echo.

set BACKUP_DIR=FirstAidApp_PlayStore_Backup_%DATE:~-4%%DATE:~-10,2%%DATE:~-7,2%
echo Creating backup folder: %BACKUP_DIR%
mkdir "%USERPROFILE%\Desktop\%BACKUP_DIR%"

echo.
echo Copying critical files...

REM Copy the signed AAB file
echo [1/4] Copying signed AAB (Play Store upload file)...
copy "app\release\app-release.aab" "%USERPROFILE%\Desktop\%BACKUP_DIR%\app-release.aab"

REM Copy keystore
echo [2/4] Copying keystore (NEVER LOSE THIS!)...
copy "firstaid-release.keystore" "%USERPROFILE%\Desktop\%BACKUP_DIR%\firstaid-release.keystore"

REM Copy keystore properties
echo [3/4] Copying keystore properties...
copy "keystore.properties" "%USERPROFILE%\Desktop\%BACKUP_DIR%\keystore.properties"

REM Copy documentation
echo [4/4] Copying Play Store readiness documentation...
copy "PLAYSTORE_READY.md" "%USERPROFILE%\Desktop\%BACKUP_DIR%\PLAYSTORE_READY.md"

echo.
echo ============================================
echo  BACKUP COMPLETE!
echo ============================================
echo.
echo Backup location:
echo %USERPROFILE%\Desktop\%BACKUP_DIR%
echo.
echo IMPORTANT FILES:
echo - app-release.aab         (Upload this to Play Store)
echo - firstaid-release.keystore (NEVER LOSE - Needed for updates)
echo - keystore.properties     (Keystore credentials)
echo - PLAYSTORE_READY.md      (Complete guide)
echo.
echo ============================================
echo NEXT STEPS:
echo 1. Upload 'app-release.aab' to Google Play Console
echo 2. Store keystore files in a SAFE location
echo 3. Follow instructions in PLAYSTORE_READY.md
echo ============================================
echo.
pause

