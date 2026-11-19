#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"

if ! command -v mvn >/dev/null 2>&1; then
    echo "Maven tidak ditemukan. Pasang Maven 3.9 atau lebih baru." >&2
    exit 1
fi

PAPER_VERSION="${PAPER_VERSION:-1.21.4-R0.1-SNAPSHOT}"

mvn -B clean package -Dpaper.version="$PAPER_VERSION"

JAR="$(find target -maxdepth 1 -name 'BookGuide-*.jar' -print | head -n 1)"
if [ -z "$JAR" ]; then
    echo "Build selesai, tetapi file jar tidak ditemukan." >&2
    exit 1
fi

echo "Jar siap: $JAR"
