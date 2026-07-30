/**
 * Contains AST node classes for expression and operand constructs.
 * These nodes represent expressions (with operators), operands (values and identifiers),
 * function calls, and literal values (strings and booleans) in Jott programs.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borneil Gope, Jatin Jain
 */
package provided;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionNode {

    private static Map<String, String> currentVariableTypes = new HashMap<>();
    private static Map<String, String> currentFunctionReturnTypes = new HashMap<>();

    public static void setCurrentVariableTypes(Map<String, String> variableTypes) {
        currentVariableTypes = new HashMap<>(variableTypes);
    }

    public static void clearCurrentVariableTypes() {
        currentVariableTypes = new HashMap<>();
    }

    public static void setCurrentFunctionReturnTypes(Map<String, String> functionReturnTypes) {
        currentFunctionReturnTypes = new HashMap<>(functionReturnTypes);
    }

    public static void clearCurrentFunctionReturnTypes() {
        currentFunctionReturnTypes = new HashMap<>();
    }

    /**
     * Represents a function call expression.
     * Contains the function header token, function name, and parameter expressions.
     * Used both as expressions (e.g., in return statements) and as statements (via FuncCallStmtNode).
     * Serializes as: ::funcName[params]
     */
    public static class FuncCallNode implements JottTree {
        public final Token header;
        public final Token name;
        public final List<ExprNode> params;

        public FuncCallNode(Token header, Token name, List<ExprNode> params) {
            this.header = header;
            this.name = name;
            this.params = params;
        }

        @Override
        public String convertToJott() {
            StringBuilder sb = new StringBuilder();
            sb.append("::").append(name.getToken()).append("[");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(params.get(i).convertToJott());
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        public String convertToJava(String className) {
            if ("print".equals(name.getToken())) {
                return "System.out.println(" + params.get(0).convertToJava(className) + ")";
            }
            if ("concat".equals(name.getToken())) {
                return params.get(0).convertToJava(className) + " + " + params.get(1).convertToJava(className);
            }
            if ("length".equals(name.getToken())) {
                return params.get(0).convertToJava(className) + ".length()";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(name.getToken()).append("(");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(params.get(i).convertToJava(className));
            }
            sb.append(")");
            return sb.toString();
        }

        @Override
        public String convertToC() {
            if ("print".equals(name.getToken())) {
                return "printf(\"%s\\n\", " + params.get(0).convertToC() + ")";
            }
            if ("concat".equals(name.getToken())) {
                return "jott_concat(" + params.get(0).convertToC() + ", " + params.get(1).convertToC() + ")";
            }
            if ("length".equals(name.getToken())) {
                return "((int)strlen(" + params.get(0).convertToC() + "))";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(name.getToken()).append("(");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(params.get(i).convertToC());
            }
            sb.append(")");
            return sb.toString();
        }

        @Override
        public String convertToPython() {
            if ("print".equals(name.getToken())) {
                return "print(" + params.get(0).convertToPython() + ")";
            }
            if ("concat".equals(name.getToken())) {
                return params.get(0).convertToPython() + " + " + params.get(1).convertToPython();
            }
            if ("length".equals(name.getToken())) {
                return "len(" + params.get(0).convertToPython() + ")";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(name.getToken()).append("(");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(params.get(i).convertToPython());
            }
            sb.append(")");
            return sb.toString();
        }

        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a binary expression or simple operand.
     * Can be a single operand (when op is null) or a binary operation (left op right).
     * Supports chaining through recursive left-associative structure.
     * Serializes as: left[op right] or left (if no operator)
     */
    public static class ExprNode implements JottTree {
        public final JottTree left;
        public final Token op;
        public final OperandNode right;

        public ExprNode(JottTree left, Token op, OperandNode right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public String convertToJott() {
            if (op == null) {
                return left.convertToJott();
            }
            return left.convertToJott() + op.getToken() + right.convertToJott();
        }

        @Override
        public String convertToJava(String className) {
            if (op == null) {
                return left.convertToJava(className);
            }
            return left.convertToJava(className) + " " + op.getToken() + " " + right.convertToJava(className);
        }

        @Override
        public String convertToC() {
            if (op == null) {
                return left.convertToC();
            }
            return left.convertToC() + " " + op.getToken() + " " + right.convertToC();
        }

        @Override
        public String convertToPython() {
            if (op == null) {
                return left.convertToPython();
            }
            String pythonOp = op.getToken();
            if ("/".equals(pythonOp)) {
                String leftType = inferTreeType(left);
                String rightType = inferOperandType(right);
                if ("Integer".equals(leftType) && "Integer".equals(rightType)) {
                    pythonOp = "//";
                }
            }
            return left.convertToPython() + " " + pythonOp + " " + right.convertToPython();
        }

        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a single operand in an expression.
     * Can be an identifier, a number (optionally negated with unary minus),
     * a function call, or a literal value.
     * Provides multiple constructors for different operand types.
     */
    public static class OperandNode implements JottTree {
        public final Token idTok;
        public final Token minusTok;
        public final Token numTok;
        public final FuncCallNode funcCall;

        public OperandNode(Token idTok) {
            this.idTok = idTok;
            this.minusTok = null;
            this.numTok = null;
            this.funcCall = null;
        }

        public OperandNode(Token minusTok, Token numTok) {
            this.idTok = null;
            this.minusTok = minusTok;
            this.numTok = numTok;
            this.funcCall = null;
        }

        public OperandNode(FuncCallNode funcCall) {
            this.idTok = null;
            this.minusTok = null;
            this.numTok = null;
            this.funcCall = funcCall;
        }

        @Override
        public String convertToJott() {
            if (funcCall != null) {
                return funcCall.convertToJott();
            }
            if (numTok != null) {
                return (minusTok != null ? "-" : "") + numTok.getToken();
            }
            return idTok.getToken();
        }

        @Override
        public String convertToJava(String className) {
            if (funcCall != null) {
                return funcCall.convertToJava(className);
            }
            if (numTok != null) {
                return (minusTok != null ? "-" : "") + numTok.getToken();
            }
            return idTok.getToken();
        }

        @Override
        public String convertToC() {
            if (funcCall != null) {
                return funcCall.convertToC();
            }
            if (numTok != null) {
                return (minusTok != null ? "-" : "") + numTok.getToken();
            }
            return idTok.getToken();
        }

        @Override
        public String convertToPython() {
            if (funcCall != null) {
                return funcCall.convertToPython();
            }
            if (numTok != null) {
                return (minusTok != null ? "-" : "") + numTok.getToken();
            }
            return idTok.getToken();
        }

        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a string literal value in Jott code.
     * Contains the token representing the complete string including quotes.
     * Used as an operand in expressions.
     * Serializes as: the exact string token
     */
    public static class StringLiteralNode implements JottTree {
        public final Token tok;

        public StringLiteralNode(Token tok) {
            this.tok = tok;
        }

        @Override
        public String convertToJott() {
            return tok.getToken();
        }

        @Override
        public String convertToJava(String className) {
            return tok.getToken();
        }

        @Override
        public String convertToC() {
            return tok.getToken();
        }

        @Override
        public String convertToPython() {
            return tok.getToken();
        }

        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a boolean literal value (True or False) in Jott code.
     * Contains the token representing the boolean keyword.
     * Used as an operand in expressions, particularly in conditions.
     * Serializes as: the exact boolean token (True or False)
     */
    public static class BoolNode implements JottTree {
        public final Token tok;

        public BoolNode(Token tok) {
            this.tok = tok;
        }

        @Override
        public String convertToJott() {
            return tok.getToken();
        }

        @Override
        public String convertToJava(String className) {
            return "True".equals(tok.getToken()) ? "true" : "false";
        }

        @Override
        public String convertToC() {
            return "True".equals(tok.getToken()) ? "true" : "false";
        }

        @Override
        public String convertToPython() {
            return tok.getToken();
        }

        @Override public boolean validateTree() { return true; }
    }

    private static String inferExprType(ExprNode expr) {
        if (expr == null) return "Unknown";
        if (expr.op == null) {
            return inferTreeType(expr.left);
        }

        if (expr.op.getTokenType() == TokenType.REL_OP) {
            return "Boolean";
        }

        String leftType = inferTreeType(expr.left);
        String rightType = inferOperandType(expr.right);

        if ("String".equals(leftType) || "String".equals(rightType)) {
            return "String";
        }
        if ("Double".equals(leftType) || "Double".equals(rightType)) {
            return "Double";
        }
        if ("Integer".equals(leftType) && "Integer".equals(rightType)) {
            return "Integer";
        }
        return "Unknown";
    }

    private static String inferTreeType(JottTree tree) {
        if (tree == null) return "Unknown";
        if (tree instanceof ExprNode) {
            return inferExprType((ExprNode) tree);
        }
        if (tree instanceof StringLiteralNode) return "String";
        if (tree instanceof BoolNode) return "Boolean";
        if (tree instanceof OperandNode) {
            return inferOperandType((OperandNode) tree);
        }
        return "Unknown";
    }

    private static String inferOperandType(OperandNode op) {
        if (op == null) return "Unknown";
        if (op.numTok != null) {
            return op.numTok.getToken().contains(".") ? "Double" : "Integer";
        }
        if (op.idTok != null) {
            String idType = currentVariableTypes.get(op.idTok.getToken());
            return idType == null ? "Unknown" : idType;
        }
        if (op.funcCall != null) {
            String name = op.funcCall.name.getToken();
            if ("concat".equals(name)) return "String";
            if ("length".equals(name)) return "Integer";
            if ("print".equals(name)) return "Void";
            String fnType = currentFunctionReturnTypes.get(name);
            return fnType == null ? "Unknown" : fnType;
        }
        return "Unknown";
    }
}
