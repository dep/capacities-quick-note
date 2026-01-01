#!/bin/bash
# Build release AAB with Java 17 from Nix

# Prompt for passwords
read -sp "Enter keystore password: " KEYSTORE_PASSWORD
echo
read -sp "Enter key password (or press Enter if same as keystore): " KEY_PASSWORD
echo

# Use keystore password if key password is empty
if [ -z "$KEY_PASSWORD" ]; then
    KEY_PASSWORD="$KEYSTORE_PASSWORD"
fi

# Export passwords
export KEYSTORE_PASSWORD
export KEY_PASSWORD

# Load Nix
source /nix/var/nix/profiles/default/etc/profile.d/nix-daemon.sh

# Build with Java 17
echo ""
echo "Building release AAB with Java 17..."
nix-shell -p jdk17 --run "./gradlew bundleRelease"

# Show results
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "Release AAB location:"
    ls -lh app/build/outputs/bundle/release/app-release.aab
else
    echo ""
    echo "❌ Build failed. Check errors above."
    exit 1
fi
