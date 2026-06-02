@echo off
echo ========================================
echo   Pushing Compiled APK to GitHub
echo ========================================
echo.

:: Stage the files
echo 1. Staging releases/app-release.apk and README.md...
git add releases/app-release.apk
git add README.md
echo.

:: Commit the changes
echo 2. Committing changes...
git commit -m "chore: Add compiled release APK and update README for students"
echo.

:: Push the changes to GitHub
echo 3. Pushing to GitHub...
git push https://github.com/malfiya1212/ORTHODOXAPP2.git ortho
echo.

echo ========================================
echo   DONE! Check your GitHub repository.
echo ========================================
pause
