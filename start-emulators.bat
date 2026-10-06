@echo off
echo ===================================================
echo Starting Firebase Local Emulators (Auth, Storage, Data Connect, UI)
echo Connected to local PostgreSQL: new-storebook-database
echo ===================================================

echo Forwarding ports to connected Android device (if any)...
adb reverse tcp:9099 tcp:9099 2>nul
adb reverse tcp:9199 tcp:9199 2>nul
adb reverse tcp:9399 tcp:9399 2>nul
adb reverse tcp:4000 tcp:4000 2>nul

set FIREBASE_DATACONNECT_POSTGRESQL_STRING=postgresql://postgres:postgres@127.0.0.1:5432/new-storebook-database?sslmode=disable
firebase emulators:start --only auth,storage,dataconnect,ui
