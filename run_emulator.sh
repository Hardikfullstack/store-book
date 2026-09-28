#!/usr/bin/env bash
set -e

# Fix Android AGP environment variable conflict
unset ANDROID_PREFS_ROOT

# Set JAVA_HOME if /opt/android-studio/jbr exists
if [ -d "/opt/android-studio/jbr" ]; then
    export JAVA_HOME="/opt/android-studio/jbr"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# Set ANDROID_HOME & ANDROID_USER_HOME
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_USER_HOME="$HOME/.android"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

EMULATOR_BIN="$ANDROID_HOME/emulator/emulator"
AVD_NAME="Pixel_7"

echo "Checking ADB devices..."
if ! adb devices | grep -q "emulator-"; then
    echo "Starting emulator ($AVD_NAME)..."
    "$EMULATOR_BIN" -avd "$AVD_NAME" -netdelay none -netspeed full > /dev/null 2>&1 &
    echo "Waiting for emulator to connect..."
    adb wait-for-device
    echo "Waiting for system boot to complete..."
    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        sleep 1
    done
    echo "Emulator booted successfully!"
else
    echo "Emulator is already running."
fi

echo "Building and installing debug app..."
./gradlew :app:installDebug

echo "Launching StoreBook MainActivity..."
adb shell am start -n com.storebook.inventoryapp/.MainActivity

echo "App running on emulator!"
