@echo off
echo ========================================
echo   Tewahedo Connect - GitHub Push Script
echo ========================================
echo.
echo 1. Staging files...
git add .
echo.
echo 2. Committing changes...
git commit -m "Final MAD project submission - Stabilized & Documented"
echo.
echo 3. Pushing to GitHub...
git push https://github.com/malfiya1212/ORTHODOXAPP2.git ortho
echo.
echo ========================================
echo   DONE! Check your GitHub repository.
echo ========================================
pause
