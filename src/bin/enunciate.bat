@echo off

rem Put some effort into finding ENUNCIATE_HOME if not set.  (Copied from ant's batch file).

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_ENUNCIATE_HOME=%~dp0..

if "%ENUNCIATE_HOME%"=="" set ENUNCIATE_HOME=%DEFAULT_ENUNCIATE_HOME%
set DEFAULT_ENUNCIATE_HOME=

if "%ENUNCIATE_JAVA_HOME%" == "" set ENUNCIATE_JAVA_HOME=%JAVA_HOME%
if "%ENUNCIATE_JAVA_HOME%" == "" goto noJavaHome
goto enunciate

:noJavaHome
echo ENUNCIATE_JAVA_HOME or JAVA_HOME is set incorrectly or could not be located.  Please set one of these.
goto end

:enunciate
%ENUNCIATE_JAVA_HOME%\bin\java -cp {WINDOWS_CLASSPATH};%ENUNCIATE_JAVA_HOME%\lib\tools.jar org.codehaus.enunciate.main.Main %*

:end