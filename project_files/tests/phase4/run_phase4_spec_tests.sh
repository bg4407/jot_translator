#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_DIR"

mkdir -p bin
javac -d bin provided/*.java testers/*.java

pass_count=0
fail_count=0

pass() {
  echo "PASS: $1"
  pass_count=$((pass_count + 1))
}

fail() {
  echo "FAIL: $1"
  fail_count=$((fail_count + 1))
}

assert_eq() {
  local name="$1"
  local expected="$2"
  local actual="$3"
  if [[ "$actual" == "$expected" ]]; then
    pass "$name"
  else
    fail "$name"
    echo "  expected: [$expected]"
    echo "  actual:   [$actual]"
  fi
}

assert_contains() {
  local name="$1"
  local needle="$2"
  local haystack_file="$3"
  if grep -q "$needle" "$haystack_file"; then
    pass "$name"
  else
    fail "$name"
    echo "  missing text: $needle"
    echo "  file: $haystack_file"
  fi
}

# 1) a/b with Integer a=5,b=2 in Python -> confirm 2 not 2.5
case1_dir="tests/phase4/spec_cases/int_division_python"
java -cp bin provided.Jott "$case1_dir/int_division_python.jott" "$case1_dir/int_division_python.py" Python
out1="$(python3 "$case1_dir/int_division_python.py" | tr -d '\r')"
assert_eq "Python Integer division truncates (5/2 -> 2)" "2" "$out1"

# 2) bar[s:String] printing String param in C -> no segfault, correct %s output
case2_dir="tests/phase4/spec_cases/c_string_param_print"
java -cp bin provided.Jott "$case2_dir/c_string_param_print.jott" "$case2_dir/c_string_param_print.c" C
gcc "$case2_dir/c_string_param_print.c" -o "$case2_dir/c_string_param_print.out"
out2="$("$case2_dir/c_string_param_print.out" 2>/dev/null || true)"
out2="$(printf '%s' "$out2" | tr -d '\r')"
assert_eq "C String param print emits correct output" "hello" "$out2"

# 3) Boolean variable print in C -> True/False not 1/0
case3_dir="tests/phase4/spec_cases/c_boolean_var_print"
java -cp bin provided.Jott "$case3_dir/c_boolean_var_print.jott" "$case3_dir/c_boolean_var_print.c" C
gcc "$case3_dir/c_boolean_var_print.c" -o "$case3_dir/c_boolean_var_print.out"
out3="$("$case3_dir/c_boolean_var_print.out" 2>/dev/null || true)"
out3="$(printf '%s' "$out3" | tr -d '\r')"
assert_eq "C Boolean variable print emits True/False" $'True\nFalse' "$out3"

# 4) Nested if/while inside function in Python -> no IndentationError and correct run
case4_dir="tests/phase4/spec_cases/python_nested_if_while"
java -cp bin provided.Jott "$case4_dir/python_nested_if_while.jott" "$case4_dir/python_nested_if_while.py" Python
out4="$(python3 "$case4_dir/python_nested_if_while.py" | tr -d '\r')"
assert_eq "Python nested If/While runs without indentation errors" $'edge\nmid\nedge' "$out4"

# 5) C main -> confirm return 1;
case5_dir="tests/phase4/spec_cases/c_main_return_one"
java -cp bin provided.Jott "$case5_dir/c_main_return_one.jott" "$case5_dir/c_main_return_one.c" C
assert_contains "Generated C main returns 1" "return 1;" "$case5_dir/c_main_return_one.c"

echo
printf 'Summary: PASS=%d FAIL=%d\n' "$pass_count" "$fail_count"

if [[ $fail_count -gt 0 ]]; then
  exit 1
fi
