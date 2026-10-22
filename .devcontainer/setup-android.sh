#!/usr/bin/env bash
set -euo pipefail

echo "=== Installing Android SDK ==="

export ANDROID_HOME="${HOME}/Android/Sdk"
export ANDROID_SDK_ROOT="${ANDROID_HOME}"
mkdir -p "${ANDROID_HOME}/cmdline-tools"

CMDTOOLS_ZIP="commandlinetools-linux-11076708_latest.zip"
if [ ! -d "${ANDROID_HOME}/cmdline-tools/latest" ]; then
  cd /tmp
  wget -q "https://dl.google.com/android/repository/${CMDTOOLS_ZIP}"
  unzip -q "${CMDTOOLS_ZIP}"
  rm -rf "${ANDROID_HOME}/cmdline-tools/latest"
  mkdir -p "${ANDROID_HOME}/cmdline-tools/latest"
  # zip extracts to cmdline-tools/
  mv cmdline-tools/* "${ANDROID_HOME}/cmdline-tools/latest/" || true
  # some zips nest differently
  if [ -d /tmp/cmdline-tools/bin ]; then
    mv /tmp/cmdline-tools/* "${ANDROID_HOME}/cmdline-tools/latest/"
  fi
  rm -f "${CMDTOOLS_ZIP}"
fi

export PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

yes | sdkmanager --licenses >/dev/null || true
sdkmanager \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;35.0.0"

# Persist env for later shells
if ! grep -q ANDROID_HOME "${HOME}/.bashrc" 2>/dev/null; then
  cat >> "${HOME}/.bashrc" <<EOF
export ANDROID_HOME=\${HOME}/Android/Sdk
export ANDROID_SDK_ROOT=\${ANDROID_HOME}
export PATH=\${PATH}:\${ANDROID_HOME}/cmdline-tools/latest/bin:\${ANDROID_HOME}/platform-tools
EOF
fi

cd "${CODESPACE_VSCODE_FOLDER:-$(pwd)}"

# Bootstrap Gradle wrapper if jar missing
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
  echo "=== Generating Gradle wrapper ==="
  gradle wrapper --gradle-version 8.9 || true
fi

echo "=== Android SDK ready ==="
echo "ANDROID_HOME=${ANDROID_HOME}"
echo "Run: ./gradlew :app:assembleDebug"
