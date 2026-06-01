@echo off
echo ========================================
echo   Building and Pushing Release APK
echo ========================================
echo.
echo 1. Building Release APK...
call gradlew.bat assembleRelease
echo.
echo 2. Copying APK to releases directory...
if not exist releases mkdir releases
copy /Y app\build\outputs\apk\release\app-release-unsigned.apk releases\app-release.apk
echo.
echo 3. Staging files...
git add releases/app-release.apk
git add README.md
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
