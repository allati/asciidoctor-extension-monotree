@ECHO OFF

SET WD=%CD%
SET SD=%~dp0
SET PARAMS=%*

cd "%SD%"

call mvnw -N io.takari:maven:0.7.6:wrapper %PARAMS%

cd "%WD%"

PAUSE
