#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_DIR"

mkdir -p bin
javac -d bin provided/*.java testers/*.java

pass_count=0
fail_count=0
skip_count=0

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
  roundtrip_out="$dir/$stem-new.jott"

  echo "Running subtest: $src"

  java -cp bin provided.Jott "$src" "$java_out" Java
  java -cp bin provided.Jott "$src" "$c_out" C
  java -cp bin provided.Jott "$src" "$py_out" Python
  java -cp bin provided.Jott "$src" "$canonical_out" Jott

  if [[ -n "${JOTT_REVERSE_JAVA_TOOL:-}" ]]; then
    "$JOTT_REVERSE_JAVA_TOOL" "$java_out" "$roundtrip_out"

    if cmp -s "$canonical_out" "$roundtrip_out"; then
      echo "  PASS: $roundtrip_out matches canonical Jott"
      pass_count=$((pass_count + 1))
    else
      echo "  FAIL: $roundtrip_out does not match canonical Jott"
      fail_count=$((fail_count + 1))
    fi
  else
    echo "  SKIP: Java->Jott reverse step (set JOTT_REVERSE_JAVA_TOOL to enable)"
    skip_count=$((skip_count + 1))
  fi

done

echo
echo "Summary: PASS=$pass_count FAIL=$fail_count SKIP=$skip_count"

if [[ $fail_count -gt 0 ]]; then
  exit 1
fi
