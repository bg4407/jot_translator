/**
 * Contains AST node classes for statement-level constructs.
 * These nodes represent individual statements that can appear in a Jott program,
 * including return statements, assignments, and function call statements.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borneil Gope, Jatin Jain
 */
package provided;

public class StmtNode {

    /**
     * Represents a return statement in Jott.
     * Contains the return token and the expression to be returned.
     * Serializes as: Return expr;
     */
    public static class ReturnStmtNode implements JottTree {
        public final Token returnToken;
        public final ExpressionNode.ExprNode expr;

        public ReturnStmtNode(Token returnToken, ExpressionNode.ExprNode expr) {
            this.returnToken = returnToken;
            this.expr = expr;
        }

        @Override
        public String convertToJott() {
            return "Return " + expr.convertToJott() + ";";
        }

        @Override
        public String convertToJava(String className) {
            return expr == null ? "return;" : "return " + expr.convertToJava(className) + ";";
        }

        @Override
        public String convertToC() {
            return expr == null ? "return;" : "return " + expr.convertToC() + ";";
        }

        @Override
        public String convertToPython() {
            return expr == null ? "return" : "return " + expr.convertToPython();
        }

        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a variable assignment statement.
     * Contains the variable identifier and the expression to be assigned.
     * Serializes as: id=expr;
     */
    public static class AsmtNode implements JottTree {
        public final Token id;
        public final ExpressionNode.ExprNode expr;

        public AsmtNode(Token id, ExpressionNode.ExprNode expr) {
            this.id = id;
            this.expr = expr;
        }

        @Override
        public String convertToJott() {
            return id.getToken() + "=" + expr.convertToJott() + ";";
        }

        @Override
        public String convertToJava(String className) {
            return id.getToken() + " = " + expr.convertToJava(className) + ";";
        }

        @Override
        public String convertToC() {
            return id.getToken() + " = " + expr.convertToC() + ";";
        }

        @Override
        public String convertToPython() {
            return id.getToken() + " = " + expr.convertToPython();
        }

        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a function call used as a statement.
     * Wraps a FuncCallNode and appends a semicolon for statement-level serialization.
     * Distinguishes between expressions (no semicolon) and statements (with semicolon).
     * Serializes as: ::funcName[params];
     */
    public static class FuncCallStmtNode implements JottTree {
        public final ExpressionNode.FuncCallNode funcCall;

        public FuncCallStmtNode(ExpressionNode.FuncCallNode funcCall) {
            this.funcCall = funcCall;
        }

        @Override
        public String convertToJott() {
            return funcCall.convertToJott() + ";";
        }

        @Override
        public String convertToJava(String className) {
            if ("print".equals(funcCall.name.getToken())) {
                return "System.out.println(" + funcCall.params.get(0).convertToJava(className) + ");";
            }
            return funcCall.convertToJava(className) + ";";
        }

        @Override
        public String convertToC() {
            if ("print".equals(funcCall.name.getToken())) {
                return printCStatement(funcCall.params.get(0));
            }
            return funcCall.convertToC() + ";";
        }

        @Override
        public String convertToPython() {
            if ("print".equals(funcCall.name.getToken())) {
                return "print(" + funcCall.params.get(0).convertToPython() + ")";
            }
            return funcCall.convertToPython() + "";
        }

        @Override public boolean validateTree() { return true; }

        private String printCStatement(ExpressionNode.ExprNode expr) {
            String inner = expr.convertToC();
            String typeHint = inferType(expr);
            if ("String".equals(typeHint)) {
                return "printf(\"%s\\n\", " + inner + ");";
            }
            if ("Boolean".equals(typeHint)) {
                return "printf(\"%s\\n\", " + inner + " ? \"True\" : \"False\");";
            }
            if ("Double".equals(typeHint)) {
                return "printf(\"%f\\n\", " + inner + ");";
            }
            return "printf(\"%d\\n\", " + inner + ");";
        }

        private String inferType(ExpressionNode.ExprNode expr) {
            if (expr == null) return "Integer";
            if (expr.left instanceof ExpressionNode.StringLiteralNode) return "String";
            if (expr.left instanceof ExpressionNode.BoolNode) return "Boolean";
            if (expr.left instanceof ExpressionNode.OperandNode) {
                ExpressionNode.OperandNode op = (ExpressionNode.OperandNode) expr.left;
                if (op.numTok != null) {
                    return op.numTok.getToken().contains(".") ? "Double" : "Integer";
                }
                if (op.idTok != null) {
                    return "Unknown";
                }
            }
            if (expr.op != null) {
                return expr.op.getTokenType() == TokenType.REL_OP ? "Boolean" : "Integer";
            }
            return "Integer";
        }
    }
}
