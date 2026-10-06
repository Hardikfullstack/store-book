@echo off
echo ===================================================
echo Setting up ADB reverse port forwarding for Emulators
echo ===================================================

adb reverse tcp:9099 tcp:9099
adb reverse tcp:9199 tcp:9199
adb reverse tcp:9399 tcp:9399
adb reverse tcp:4000 tcp:4000

echo.
echo Ports 9099 (Auth), 9199 (Storage), 9399 (Data Connect), and 4000 (UI) forwarded.
echo Physical device can now access emulators on 127.0.0.1.
