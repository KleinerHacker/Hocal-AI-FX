@rem
@rem Start script for the Hocal AI UI.
@rem All application and dependency JARs live next to this script in "libs".
@rem
@echo off
setlocal

set APP_HOME=%~dp0

if defined JAVA_HOME (
  set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
) else (
  set JAVA_CMD=java
)

%JAVA_CMD% --module-path "%APP_HOME%libs" --add-modules ALL-MODULE-PATH --enable-native-access=javafx.graphics --module org.pcsoft.app.hocal.ui/org.pcsoft.app.hocal.app.LauncherKt %*

endlocal
