@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
echo Running gradle build with stacktrace and saving output to gradle_error.txt...
call gradlew.bat assembleDebug --stacktrace > gradle_error.txt 2>&1
echo Done! Please tell the assistant you ran this script.
pause
