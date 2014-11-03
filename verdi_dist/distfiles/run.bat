@ECHO OFF

SET BATCHFILE=%~f2

SET VERDI_HOME=C:\\VERDI_test\\VERDI_1.5.0_test1103_v80
CD .\plugins\bootstrap
set JAVADIR=%VERDI_HOME%\jre1.7.0
SET JAVA=%JAVADIR%\bin\java
set CLASSPATH=%JAVADIR%/bin/*;%JAVADIR%/lib/*;%VERDI_HOME%/plugins/bootstrap/bootstrap.jar;%VERDI_HOME%/plugins/bootstrap/lib/;%VERDI_HOME%/plugins/bootstrap/lib/saf.core.runtime.jar;%VERDI_HOME%/plugins/bootstrap/lib/*;%VERDI_HOME%/plugins/core/lib/*

REM set PATH based on 32/64 Windows
set bit64=n

if /I %Processor_Architecture%==AMD64 set bit64=y
if /I "%PROCESSOR_ARCHITEW6432%"=="AMD64" set bit64=y

if /I %bit64%==y GOTO proc64
set PATH=%PATH%;%VERDI_HOME%\\plugins\\core\\lib\\Win32
REM echo set to Win32 path
GOTO proc32

:proc64
set PATH=%PATH%;%VERDI_HOME%\\plugins\\core\\lib\\Win64
REM echo set to Win64 path

:proc32
REM path has been reset for DLLs; continue
set JAVACMD=%JAVA% -Xmx1024M -classpath %CLASSPATH% saf.core.runtime.Boot

IF "%1" == "-b" GOTO scripting

IF "%1" == "-batch" GOTO scripting

%JAVACMD% %*
GOTO end

:scripting
%JAVACMD% %1 %BATCHFILE%

:end
CD ..\..\