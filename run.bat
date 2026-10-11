@echo off
echo Compiling the Personal Finance Tracker...
mkdir bin 2>nul
javac -d bin src/com/financetracker/model/*.java src/com/financetracker/exception/*.java src/com/financetracker/service/*.java src/com/financetracker/util/*.java src/com/financetracker/*.java src/com/financetracker/ui/*.java src/com/financetracker/ui/components/*.java src/com/financetracker/ui/dialogs/*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting the Application...
java -cp bin com.financetracker.Main

pause
