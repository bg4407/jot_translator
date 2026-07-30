/**
 * Contains AST node classes for expression and operand constructs.
 * These nodes represent expressions (with operators), operands (values and identifiers),
 * function calls, and literal values (strings and booleans) in Jott programs.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borneil Gope, Jatin Jain
 */
package provided;

import java.util.List;

public class ExpressionNode {

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
                return "strcat(" + params.get(0).convertToC() + ", " + params.get(1).convertToC() + ")";
            }
            if ("length".equals(name.getToken())) {
                return "strlen(" + params.get(0).convertToC() + ")";
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
            return left.convertToPython() + " " + op.getToken() + " " + right.convertToPython();
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
}
