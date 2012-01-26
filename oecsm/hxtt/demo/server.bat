REM Please modify JDK path for your computer
REM Please modify url for your computer

SET TEMPCLASSPATH=%CLASSPATH%
SET TEMPPATH=%PATH%

set PATH=\j2sdk1.4.2_04\bin;%PATH%
set CLASSPATH=\j2sdk1.4.2_04\lib\

SET CLASSPATH=%CLASSPATH%;..\..\lib\Access_JDBC30.jar;.\classes

REM read Remote Access Questions section of faq.html if you wish to run as Windows service or Linux daemon
java -Djava.security.policy=policy com.hxtt.sql.admin.Admin

SET CLASSPATH=%TEMPCLASSPATH%
SET PATH=%TEMPPATH%
