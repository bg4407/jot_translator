#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_DIR"

mkdir -p bin
javac -d bin provided/*.java testers/*.java

pass_count=0

mapfile -t sources < <(find tests/roundtrip -mindepth 2 -maxdepth 2 -type f -name '*.jott' \
  ! -name '*-new.jott' ! -name '*-canonical.jott' | sort)

if [[ ${#sources[@]} -eq 0 ]]; then
  echo "No source .jott files found under tests/roundtrip/*/."
  exit 1
fi

for src in "${sources[@]}"; do
  dir="$(dirname "$src")"
  stem="$(basename "$src" .jott)"

  java_out="$dir/$stem.java"
  c_out="$dir/$stem.c"
  py_out="$dir/$stem.py"
  canonical_out="$dir/$stem-canonical.jott"

  echo "Running subtest: $src"

  java -cp bin provided.Jott "$src" "$java_out" Java
  java -cp bin provided.Jott "$src" "$c_out" C
  java -cp bin provided.Jott "$src" "$py_out" Python
  java -cp bin provided.Jott "$src" "$canonical_out" Jott
  pass_count=$((pass_count + 1))
  echo "  PASS: generated .java/.c/.py/.jott fixtures"

done

echo
echo "Summary: PASS=$pass_count FAIL=0"
