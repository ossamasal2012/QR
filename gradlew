#!/usr/bin/env sh
# Lightweight Gradle bootstrap for this project; downloads the pinned, checksum-verified distribution.
set -eu
GRADLE_VERSION="8.13"
EXPECTED_SHA256="20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78"
GRADLE_HOME_BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/qr-vision-wrapper"
GRADLE_HOME_DIR="$GRADLE_HOME_BASE/gradle-$GRADLE_VERSION"
if [ ! -x "$GRADLE_HOME_DIR/bin/gradle" ]; then
  mkdir -p "$GRADLE_HOME_BASE"
  ZIP="$GRADLE_HOME_BASE/gradle-$GRADLE_VERSION-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" --output "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  else
    echo "curl or wget is required to download Gradle $GRADLE_VERSION." >&2
    exit 1
  fi
  if command -v sha256sum >/dev/null 2>&1; then
    echo "$EXPECTED_SHA256  $ZIP" | sha256sum --check --status || { echo "Gradle distribution checksum mismatch." >&2; rm -f "$ZIP"; exit 1; }
  elif command -v shasum >/dev/null 2>&1; then
    ACTUAL_SHA256="$(shasum -a 256 "$ZIP" | awk '{print $1}')"
    [ "$ACTUAL_SHA256" = "$EXPECTED_SHA256" ] || { echo "Gradle distribution checksum mismatch." >&2; rm -f "$ZIP"; exit 1; }
  else
    echo "sha256sum or shasum is required to verify the Gradle distribution." >&2
    rm -f "$ZIP"
    exit 1
  fi
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required." >&2; exit 1; }
  unzip -q -o "$ZIP" -d "$GRADLE_HOME_BASE"
  rm -f "$ZIP"
fi
exec "$GRADLE_HOME_DIR/bin/gradle" "$@"
