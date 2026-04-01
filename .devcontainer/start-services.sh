#!/bin/bash
# start-services.sh — Démarre VNC, noVNC et l'émulateur Android
set -euo pipefail

export ANDROID_HOME="/home/codespace/android-sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
export DISPLAY=:1

# --- VNC Server ---
echo ">>> Starting VNC server on :1 (5901)..."
# Set VNC password (empty = no password prompt in noVNC)
mkdir -p "$HOME/.vnc"
echo "android" | vncpasswd -f > "$HOME/.vnc/passwd"
chmod 600 "$HOME/.vnc/passwd"

# Kill existing if any
vncserver -kill :1 2>/dev/null || true
sleep 1

vncserver :1 -geometry 1280x720 -depth 24 -SecurityTypes VncAuth -PasswordFile "$HOME/.vnc/passwd"

# --- XFCE4 desktop ---
echo ">>> Starting XFCE4 session..."
export XDG_SESSION_TYPE=x11
nohup startxfce4 > /tmp/xfce4.log 2>&1 &
sleep 2

# --- noVNC (websockify) ---
echo ">>> Starting noVNC on port 6080..."
nohup websockify --web /usr/share/novnc 6080 localhost:5901 > /tmp/novnc.log 2>&1 &
sleep 1

# --- Android Emulator ---
echo ">>> Starting Android emulator (pixel)..."
nohup emulator -avd pixel \
    -no-audio \
    -gpu swiftshader_indirect \
    -no-snapshot \
    -skin 540x960 \
    -memory 2048 \
    -no-boot-anim \
    -wipe-data \
    > /tmp/emulator.log 2>&1 &

echo ">>> Waiting for emulator to boot..."
adb wait-for-device
timeout 180 adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 2; done' 2>/dev/null || echo ">>> Warning: boot timeout (emulator may still be starting)"

echo ""
echo "============================================"
echo "  Environment ready!"
echo "  noVNC: http://localhost:6080/vnc.html"
echo "  VNC password: android"
echo "  Emulator: emulator-5554"
echo "============================================"
