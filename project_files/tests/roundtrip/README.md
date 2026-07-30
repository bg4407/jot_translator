# Round-Trip Test Harness

This directory holds structured translation tests by subtest folder.

## Pattern

Each subtest has a source Jott file and generated outputs:

- `fileN.jott` (source)
- `fileN.java` (generated)
- `fileN.c` (generated)
- `fileN.py` (generated)
- `fileN-canonical.jott` (canonical Jott emitted by this translator from `fileN.jott`)

## Scope

This translator supports one-way generation only:

- `Jott -> {Jott, Java, C, Python}`

It does **not** parse Java/C/Python as input.

## Run all round-trip tests

From `project_files`:

```bash
bash tests/roundtrip/run_roundtrip.sh
```
