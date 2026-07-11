package provided;

import java.util.ArrayList;

public class Jott {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Jott <input.jott> <output_file> <Jott|Java|C|Python>");
            return;
        }

        // Phase 1: Tokenizing
        ArrayList<Token> tokens = JottTokenizer.tokenize(args[0]);
        if (tokens == null) return;

        // Phase 2: Parsing (syntax only)
        JottTree tree = JottParser.parse(tokens);
        if (tree == null) return;

        // Phase 3: Semantic Analysis (separate step from parsing)
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        if (!(tree instanceof ProgramStructureNode.ProgramNode)) {
            System.err.println("Internal error: root is not a ProgramNode");
            return;
        }
        if (!analyzer.validateProgram((ProgramStructureNode.ProgramNode) tree)) {
            return; // semantic errors already reported by the analyzer
        }

        System.out.println("Valid Jott program.");
    }
}