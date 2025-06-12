@echo off
echo Запуск Аукціонної Системи...
echo.

REM Перевірка наявності Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Помилка: Java не встановлена або недоступна.
    echo Встановіть Java 17 або новішу версію.
    pause
    exit /b 1
)

REM Запуск програми
echo Запуск програми...
java -jar auction-system-1.0-SNAPSHOT.jar

REM Якщо програма завершилась з помилкою
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Програма завершилась з помилкою. Перевірте налаштування бази даних.
    pause
) 