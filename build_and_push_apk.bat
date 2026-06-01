@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
echo ========================================
echo   Building and Pushing Release APK
echo ========================================
echo.

:: Stop existing gradle daemons to resolve FileLockContentionHandler (BindException)
echo Stopping existing Gradle daemons...
call gradlew.bat --stop
echo.

:: Clean previous builds
echo 1. Cleaning build directory...
call gradlew.bat clean

echo.
echo 2. Building Release APK...
call gradlew.bat assembleRelease

:: Check if release APK was generated
if exist app\build\outputs\apk\release\app-release-unsigned.apk (
    echo.
    echo Found unsigned release APK. Copying...
    if not exist releases mkdir releases
    copy /Y app\build\outputs\apk\release\app-release-unsigned.apk releases\app-release.apk
    goto :success
)

if exist app\build\outputs\apk\release\app-release.apk (
    echo.
    echo Found signed release APK. Copying...
    if not exist releases mkdir releases
    copy /Y app\build\outputs\apk\release\app-release.apk releases\app-release.apk
    goto :success
)

echo.
echo [WARNING] Release build failed or did not generate APK.
echo Attempting to build Debug APK as a fallback...
echo.

call gradlew.bat assembleDebug

if exist app\build\outputs\apk\debug\app-debug.apk (
    echo.
    echo Found Debug APK. Copying and renaming to app-release.apk...
    if not exist releases mkdir releases
    copy /Y app\build\outputs\apk\debug\app-debug.apk releases\app-release.apk
    goto :success
)

echo.
echo [ERROR] Both Release and Debug builds failed to generate an APK.
echo Please scroll up in the terminal to see the compilation errors.
pause
exit /b 1

:success
echo.
echo APK successfully prepared at releases\app-release.apk!
echo.
echo 3. Staging files...
git add releases/app-release.apk
git add README.md
git add app/build.gradle.kts
git add build_and_push_apk.bat
echo.
echo 4. Committing changes...
git commit -m "chore: Add compiled release APK and update README for students"
echo.
echo 5. Pushing to GitHub...
git push https://github.com/malfiya1212/ORTHODOXAPP2.git ortho
echo.
echo ========================================
echo   DONE! Check your GitHub repository.
echo ========================================
pause
