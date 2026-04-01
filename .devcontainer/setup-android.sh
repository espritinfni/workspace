#!/bin/bash
# setup-android.sh — Installe le SDK Android et crée l'AVD
# Versions figées pour reproductibilité
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-/home/codespace/android-sdk}"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

# --- Android SDK ---
if [ ! -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
    echo ">>> Installing Android SDK cmdline-tools..."
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    cd /tmp
    wget -q "$CMDLINE_TOOLS_URL" -O cmdline-tools.zip
    unzip -q cmdline-tools.zip
    mv cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
    rm cmdline-tools.zip
else
    echo ">>> Android SDK cmdline-tools already installed."
fi

export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# --- Accept licenses ---
yes | sdkmanager --licenses > /dev/null 2>&1 || true

# --- SDK packages (versions exactes) ---
echo ">>> Installing SDK packages..."
sdkmanager \
    "platform-tools" \
    "build-tools;35.0.0" \
    "platforms;android-35" \
    "emulator" \
    "system-images;android-35;google_apis;x86_64"

# --- AVD "pixel" ---
if [ ! -d "$HOME/.android/avd/pixel.avd" ]; then
    echo ">>> Creating AVD 'pixel'..."
    echo "no" | avdmanager create avd \
        -n pixel \
        -k "system-images;android-35;google_apis;x86_64" \
        --device "pixel" \
        --sdcard 512M
else
    echo ">>> AVD 'pixel' already exists."
fi

echo ">>> Android SDK setup complete."
