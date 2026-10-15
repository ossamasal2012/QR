@echo off
setlocal EnableExtensions
set GRADLE_VERSION=8.13
set EXPECTED_SHA256=20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78
if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set GRADLE_HOME_BASE=%GRADLE_USER_HOME%\qr-vision-wrapper
set GRADLE_HOME_DIR=%GRADLE_HOME_BASE%\gradle-%GRADLE_VERSION%
if not exist "%GRADLE_HOME_DIR%\bin\gradle.bat" (
  if not exist "%GRADLE_HOME_BASE%" mkdir "%GRADLE_HOME_BASE%"
  set ZIP=%GRADLE_HOME_BASE%\gradle-%GRADLE_VERSION%-bin.zip
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_HOME_BASE%\gradle-%GRADLE_VERSION%-bin.zip'; $h=(Get-FileHash '%GRADLE_HOME_BASE%\gradle-%GRADLE_VERSION%-bin.zip' -Algorithm SHA256).Hash.ToLower(); if ($h -ne '%EXPECTED_SHA256%') { throw 'Gradle distribution checksum mismatch' }; Expand-Archive -Force '%GRADLE_HOME_BASE%\gradle-%GRADLE_VERSION%-bin.zip' '%GRADLE_HOME_BASE%'; Remove-Item '%GRADLE_HOME_BASE%\gradle-%GRADLE_VERSION%-bin.zip'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_HOME_DIR%\bin\gradle.bat" %*
