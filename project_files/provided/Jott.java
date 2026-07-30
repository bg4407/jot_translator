package provided;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        String language = args[2];
        String output = render(tree, language, args[1]);
        if (output == null) {
            return;
        }

        Path outputPath = Paths.get(args[1]);
        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outputPath, output.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Failed to write output file: " + e.getMessage());
        }
    }

    private static String render(JottTree tree, String language, String outputPath) {
        String className = deriveClassName(outputPath);
        switch (language) {
            case "Jott":
                return tree.convertToJott();
            case "Java":
                return tree.convertToJava(className);
            case "C":
                return tree.convertToC();
            case "Python":
                return tree.convertToPython();
            default:
                System.err.println("Unsupported target language: " + language);
                return null;
        }
    }

    private static String deriveClassName(String outputPath) {
        Path path = Paths.get(outputPath);
        String fileName = path.getFileName() == null ? "JottProgram" : path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            fileName = fileName.substring(0, dot);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        String result = sb.toString();
        if (result.isEmpty()) {
            return "JottProgram";
        }
        if (!Character.isLetter(result.charAt(0)) && result.charAt(0) != '_') {
            result = "_" + result;
        }
        return result;
    }
}