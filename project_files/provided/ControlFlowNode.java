/**
 * Contains AST node classes for control flow constructs.
 * These nodes represent conditional and looping structures in Jott programs,
 * including if/else if/else statements and while loops.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borniel Gope, Jatin Jain
 */
package provided;

import java.util.List;

public class ControlFlowNode {

    /**
     * Represents an if statement with optional else-if and else clauses.
     * Contains a condition expression and the body to execute if the condition is true.
     * May also contain a list of else-if clauses and an optional else clause.
     * Serializes as: If[cond]{body}[Elseif[...]Else{...}]
     */
    public static class IfStmtNode implements JottTree {
        public final Token ifToken;
        public final ExpressionNode.ExprNode cond;
        public final ProgramStructureNode.BodyNode body;
        public final List<ElseIfNode> elseIfs;
        public final ElseNode elseNode;

        public IfStmtNode(Token ifToken, ExpressionNode.ExprNode cond, ProgramStructureNode.BodyNode body,
                          List<ElseIfNode> elseIfs, ElseNode elseNode) {
            this.ifToken = ifToken;
            this.cond = cond;
            this.body = body;
            this.elseIfs = elseIfs;
            this.elseNode = elseNode;
        }

        @Override
        public String convertToJott() {
            StringBuilder sb = new StringBuilder();
            sb.append("If[").append(cond.convertToJott()).append("]{");
            sb.append(body.convertToJott()).append("}");
            for (ElseIfNode ei : elseIfs) {
                sb.append(ei.convertToJott());
            }
            if (elseNode != null) {
                sb.append(elseNode.convertToJott());
            }
            return sb.toString();
        }

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents an else-if clause in an if statement.
     * Contains a condition expression and the body to execute if the condition is true.
     * Used as part of chained conditional logic.
     * Serializes as: Elseif[cond]{body}
     */
    public static class ElseIfNode implements JottTree {
        public final Token elseifToken;
        public final ExpressionNode.ExprNode cond;
        public final ProgramStructureNode.BodyNode body;

        public ElseIfNode(Token elseifToken, ExpressionNode.ExprNode cond, ProgramStructureNode.BodyNode body) {
            this.elseifToken = elseifToken;
            this.cond = cond;
            this.body = body;
        }

        @Override
        public String convertToJott() {
            return "Elseif[" + cond.convertToJott() + "]{" + body.convertToJott() + "}";
        }

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents an else clause in an if statement.
     * Contains the body to execute when all preceding if/else-if conditions are false.
     * Provides default execution path for conditional statements.
     * Serializes as: Else{body}
     */
    public static class ElseNode implements JottTree {
        public final Token elseToken;
        public final ProgramStructureNode.BodyNode body;

        public ElseNode(Token elseToken, ProgramStructureNode.BodyNode body) {
            this.elseToken = elseToken;
            this.body = body;
        }

        @Override
        public String convertToJott() {
            return "Else{" + body.convertToJott() + "}";
        }

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a while loop statement.
     * Contains a condition expression and the body to repeatedly execute while the condition is true.
     * Manages loop iteration and termination logic.
     * Serializes as: While[cond]{body}
     */
    public static class WhileNode implements JottTree {
        public final Token whileToken;
        public final ExpressionNode.ExprNode cond;
        public final ProgramStructureNode.BodyNode body;

        public WhileNode(Token whileToken, ExpressionNode.ExprNode cond, ProgramStructureNode.BodyNode body) {
            this.whileToken = whileToken;
            this.cond = cond;
            this.body = body;
        }

        @Override
        public String convertToJott() {
            return "While[" + cond.convertToJott() + "]{" + body.convertToJott() + "}";
        }

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
        @Override public boolean validateTree() { return true; }
    }
}
