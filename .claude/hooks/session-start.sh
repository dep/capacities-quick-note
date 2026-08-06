#!/bin/bash
# SessionStart hook: install the Android SDK so Claude Code on the web can
# build the app and run the unit tests.
#
# Design notes:
#   - Runs only in Claude Code on the web (remote) sessions. Local CLI sessions
#     are expected to already have their own SDK, so we skip them entirely.
#   - Idempotent: if the SDK is already present (e.g. a cached container) it
#     just re-exports the env vars and exits.
#   - Non-interactive: licenses are accepted with `yes`.
#   - Keeps stdout quiet (only short status lines); verbose install output goes
#     to a log file so it doesn't flood the session context.
set -euo pipefail

# Only run in the remote (web) environment.
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_TOOLS_DIR="$ANDROID_HOME/cmdline-tools/latest"
SDKMANAGER="$CMDLINE_TOOLS_DIR/bin/sdkmanager"
INSTALL_LOG="/tmp/android-sdk-install.log"

# Keep these in sync with app/build.gradle.kts (compileSdk / build tools).
PLATFORM="platforms;android-36"
BUILD_TOOLS="build-tools;36.0.0"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

# Persist env vars for the rest of the session so ./gradlew can find the SDK.
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  {
    echo "export ANDROID_HOME=\"$ANDROID_HOME\""
    echo "export ANDROID_SDK_ROOT=\"$ANDROID_HOME\""
    echo "export PATH=\"$CMDLINE_TOOLS_DIR/bin:$ANDROID_HOME/platform-tools:\$PATH\""
  } >> "$CLAUDE_ENV_FILE"
fi

# Already installed (cached container)? Nothing to do.
if [ -d "$ANDROID_HOME/platforms/android-36" ] && [ -d "$ANDROID_HOME/build-tools/36.0.0" ]; then
  echo "Android SDK already present at $ANDROID_HOME"
  exit 0
fi

echo "Installing Android SDK to $ANDROID_HOME ..."

# 1. Command-line tools (provides sdkmanager).
if [ ! -x "$SDKMANAGER" ]; then
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/clt.zip" "$CMDLINE_TOOLS_URL"
  unzip -q "$tmp/clt.zip" -d "$tmp"
  rm -rf "$CMDLINE_TOOLS_DIR"
  # The archive unpacks to a top-level "cmdline-tools" dir; place it at ".../latest".
  mv "$tmp/cmdline-tools" "$CMDLINE_TOOLS_DIR"
  rm -rf "$tmp"
fi

# 2. Accept licenses (non-interactive). `yes` exits via SIGPIPE, so ignore its status.
yes | "$SDKMANAGER" --licenses > /dev/null 2>&1 || true

# 3. Install the packages this project needs.
if ! "$SDKMANAGER" "platform-tools" "$PLATFORM" "$BUILD_TOOLS" > "$INSTALL_LOG" 2>&1; then
  echo "Android SDK package install failed. Last lines of $INSTALL_LOG:" >&2
  tail -n 20 "$INSTALL_LOG" >&2
  exit 1
fi

echo "Android SDK ready at $ANDROID_HOME"
