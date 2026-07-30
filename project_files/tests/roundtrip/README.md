# Round-Trip Test Harness

This directory holds structured translation tests by subtest folder.

## Pattern

Each subtest has a source Jott file and generated outputs:

- `fileN.jott` (source)
- `fileN.java` (generated)
- `fileN.c` (generated)
- `fileN.py` (generated)
- `fileN-new.jott` (reverse round-trip output, when a reverse converter is configured)
- `fileN-canonical.jott` (canonical Jott emitted by this translator from `fileN.jott`)

## Important current limitation

The current translator only supports `Jott -> {Jott, Java, C, Python}`.
It does **not** parse Java/C/Python back into Jott by itself.

To enable the exact Java -> Jott round-trip check, set:

- `JOTT_REVERSE_JAVA_TOOL=/absolute/path/to/reverse_converter`

The tool must support this interface:

```bash
reverse_converter <input.java> <output.jott>
```

## Run all round-trip tests

From `project_files`:

```bash
bash tests/roundtrip/run_roundtrip.sh
```
