# Phase 4 Spec Regression Tests

These tests assert concrete behaviors required by the Phase 4 specification.

## Covered checks

1. Python integer division truncates for Integer operands (`5/2 -> 2`).
2. C String parameter printing runs safely and prints text output.
3. C Boolean variable printing outputs `True`/`False` (not `1`/`0`).
4. Nested `If`/`While` in Python executes without indentation errors.
5. Generated C `main` contains `return 1;`.

## Run

From `project_files`:

```bash
bash tests/phase4/run_phase4_spec_tests.sh
```
