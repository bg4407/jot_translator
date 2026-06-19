# Jott Translator - Phase 1 (Tokenizer) & Phase 2 (Parser)

## Group Members (GROUP 1)
Authors: Conner Meagher, Anindita Bhowmik, Borniel Gope, Jatin Jain


## Project Structure
- `provided/`: Top-level package containing the compiler lifecycle files (`JottTokenizer.java`, `Token.java`, `TokenType.java`, `JottTree.java`, `JottParser.java`, and node definition files).
  - `ProgramStructureNode.java`: Program-level and structural nodes
  - `StmtNode.java`: Statement-level nodes
  - `ControlFlowNode.java`: Control flow constructs (if/else/while)
  - `ExpressionNode.java`: Expression and operand nodes
- `testers/`: Contains the test runner classes (`JottTokenizerTester.java`, `JottParserTester.java`).
- `tokenizerTestCases/`: Test cases for tokenizer (Phase 1)
- `phase3testcases/`: Test cases for parser (Phase 2+)

## Building and running the code

From the `project_files` directory:

```bash
mkdir -p bin
javac -d bin provided/*.java testers/*.java
```

### Running the Tokenizer Tester (Phase 1)

```bash
cd project_files
java -cp bin testers.JottTokenizerTester
```

### Running the Parser Tester (Phase 2)

```bash
cd project_files
java -cp bin testers.JottParserTester
```

## Test Results

**Tokenizer Tests**: 12/12 
**Parser Tests**: 30/30 

## Architecture Notes

The parser uses a recursive descent approach with separate node files for better organization:
- **ProgramStructureNode**: Top-level program, function definitions, parameters, bodies
- **StmtNode**: Return statements, assignments, and function call statements (with semicolon wrapper)
- **ControlFlowNode**: If/else if/else conditionals and while loops
- **ExpressionNode**: Function calls, binary expressions, operands, and literals

All nodes implement the `JottTree` interface and support conversion to multiple target languages.

