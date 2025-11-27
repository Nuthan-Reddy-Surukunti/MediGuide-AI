@echo off
echo ========================================
echo Package Name Refactor Script
echo From: com.example.firstaidapp
echo To: com.mediguide.firstaid
echo ========================================
echo.

cd /d "C:\Users\Nuthan Reddy\FirstAidApp"

echo Step 1: Creating new directory structure...
mkdir "app\src\main\java\com\mediguide\firstaid" 2>nul

echo Step 2: Moving all source files...
xcopy "app\src\main\java\com\example\firstaidapp\*" "app\src\main\java\com\mediguide\firstaid\" /E /I /Y

echo Step 3: Deleting old directory...
rmdir /S /Q "app\src\main\java\com\example" 2>nul

echo Step 4: Updating package declarations in all Kotlin files...
powershell -Command "(Get-ChildItem -Path 'app\src\main\java\com\mediguide\firstaid' -Recurse -Filter *.kt) | ForEach-Object { (Get-Content $_.FullName) -replace 'package com.example.firstaidapp', 'package com.mediguide.firstaid' | Set-Content $_.FullName }"

echo Step 5: Updating imports in all Kotlin files...
powershell -Command "(Get-ChildItem -Path 'app\src\main\java\com\mediguide\firstaid' -Recurse -Filter *.kt) | ForEach-Object { (Get-Content $_.FullName) -replace 'import com.example.firstaidapp', 'import com.mediguide.firstaid' | Set-Content $_.FullName }"

echo.
echo ========================================
echo Refactoring Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Open Android Studio
echo 2. File -^> Sync Project with Gradle Files
echo 3. Build -^> Clean Project
echo 4. Build -^> Rebuild Project
echo.
pause

