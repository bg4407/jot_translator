# Jott Translator - Phase 1 (Tokenizer) & Phase 2 (Parser)

## Group Members (GROUP 1)
Authors: Conner Meagher, Anindita Bhowmik, Borneil Gope, Jatin Jain


## Project Structure
- `provided/`: Top-level package containing the compiler lifecycle files (`JottTokenizer.java`, `Token.java`, `TokenType.java`, `JottTree.java`, `JottParser.java`, and node definition files).
  - `ProgramStructureNode.java`: Program-level and structural nodes
  - `StmtNode.java`: Statement-level nodes
  - `ControlFlowNode.java`: Control flow constructs (if/else/while)
  - `ExpressionNode.java`: Expression and operand nodes
- `testers/`: Contains the test runner classes (`JottTokenizerTester.java`, `JottParserTester.java`).
- `tokenizerTestCases/`: Test cases for tokenizer (Phase 1)
- `phase3testcases/`: Test cases for parser (Phase 2+)


### Build and run all tests

```bash
cd /jot_translator/project_files
mkdir -p bin
javac -d bin provided/*.java testers/*.java
java -cp bin testers.JottTokenizerTester
java -cp bin testers.JottParserTester
java -cp bin testers.Phase3Tester
```

You can also run everything in one line:

```bash
cd /workspaces/jot_translator/project_files && mkdir -p bin && javac -d bin provided/*.java testers/*.java && java -cp bin testers.JottTokenizerTester && java -cp bin testers.JottParserTester && java -cp bin testers.Phase3Tester
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

### Running the Semantic Tester (Phase 3)

```bash
cd project_files
java -cp bin testers.Phase3Tester
```

### Phase 4 Full project Compilation 
```bash
cd /workspaces/jot_translator/project_files
java -cp bin provided.Jott <input.jott> <output_file> <Jott|Java|C|Python>
```

## Test Results

**Tokenizer Tests**: 12/12 
**Parser Tests**: 30/30 
**Phase 3 Tests**: 19/19

## Architecture Notes

The parser uses a recursive descent approach with separate node files for better organization:
- **ProgramStructureNode**: Top-level program, function definitions, parameters, bodies
- **StmtNode**: Return statements, assignments, and function call statements (with semicolon wrapper)
- **ControlFlowNode**: If/else if/else conditionals and while loops
- **ExpressionNode**: Function calls, binary expressions, operands, and literals

All nodes implement the `JottTree` interface and support conversion to multiple target languages.

