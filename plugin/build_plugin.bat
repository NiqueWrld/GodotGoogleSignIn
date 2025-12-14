@echo off
echo ========================================
echo Google Sign-In Plugin Build Script
echo ========================================
echo.

cd /d "%~dp0"

REM Check if godot-lib exists
if not exist "libs\godot-lib*.aar" (
    echo ERROR: godot-lib.aar not found in libs folder!
    echo.
    echo Please download it from:
    echo https://downloads.tuxfamily.org/godotengine/
    echo.
    echo Or copy it from your Godot export templates folder.
    pause
    exit /b 1
)

echo Found godot-lib.aar
echo.

REM Check for gradlew
if not exist "gradlew.bat" (
    echo Generating Gradle wrapper...
    call gradle wrapper --gradle-version 8.4
)

echo Building plugin...
call gradlew.bat assembleRelease

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo BUILD FAILED!
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.

REM Copy the AAR to plugins folder
if exist "build\outputs\aar\GoogleSignIn-release.aar" (
    echo Copying GoogleSignIn.aar to plugins folder...
    copy /Y "build\outputs\aar\GoogleSignIn-release.aar" "..\GoogleSignIn.aar"
    echo.
    echo Done! Plugin ready at: android\plugins\GoogleSignIn.aar
) else (
    echo Warning: Could not find output AAR file
)

echo.
pause
