/**
 * Contains AST node classes for statement-level constructs.
 * These nodes represent individual statements that can appear in a Jott program,
 * including return statements, assignments, and function call statements.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borniel Gope, Jatin Jain
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

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
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

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
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

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
        @Override public boolean validateTree() { return true; }
    }
}
