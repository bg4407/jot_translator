/**
 * Contains AST node classes for program-level structure and organization.
 * These nodes represent the top-level constructs of a Jott program including
 * the program itself, function definitions, parameters, and variable declarations.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borneil Gope, Jatin Jain
 */
package provided;

import java.util.List;

public class ProgramStructureNode {

    /**
     * Represents the root node of a Jott program.
     * Contains a list of function definitions that make up the complete program.
     * Responsible for converting the entire program back to Jott syntax.
     */
    public static class ProgramNode implements JottTree {
        public final List<FunctionDefNode> funcs;

        public ProgramNode(List<FunctionDefNode> funcs) {
            this.funcs = funcs;
        }

        @Override
        public String convertToJott() {
            StringBuilder sb = new StringBuilder();
            for (FunctionDefNode f : funcs) {
                sb.append(f.convertToJott());
            }
            return sb.toString();
        }

        @Override public String convertToJava(String className) { throw new UnsupportedOperationException("Not implemented: convertToJava"); }
        @Override public String convertToC() { throw new UnsupportedOperationException("Not implemented: convertToC"); }
        @Override public String convertToPython() { throw new UnsupportedOperationException("Not implemented: convertToPython"); }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a function definition in Jott.
     * Contains the function name, parameter list, return type, and function body.
     * Handles serialization in the format: Def name[params]:returnType{body}
     */
    public static class FunctionDefNode implements JottTree {
        public final Token name;
        public final List<ParamDefNode> params;
        public final String returnType;
        public final FBodyNode fBody;

        public FunctionDefNode(Token name, List<ParamDefNode> params, String returnType, FBodyNode fBody) {
            this.name = name;
            this.params = params;
            this.returnType = returnType;
            this.fBody = fBody;
        }

        @Override
        public String convertToJott() {
            StringBuilder sb = new StringBuilder();
            sb.append("Def ").append(name.getToken()).append("[");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(params.get(i).convertToJott());
            }
            sb.append("]:").append(returnType).append("{");
            sb.append(fBody.convertToJott());
            sb.append("}");
            return sb.toString();
        }

        @Override public String convertToJava(String className) { throw new UnsupportedOperationException("Not implemented: convertToJava"); }
        @Override public String convertToC() { throw new UnsupportedOperationException("Not implemented: convertToC"); }
        @Override public String convertToPython() { throw new UnsupportedOperationException("Not implemented: convertToPython"); }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a single parameter definition in a function signature.
     * Contains the parameter name and its type annotation.
     * Serializes as: name:type
     */
    public static class ParamDefNode implements JottTree {
        public final Token name;
        public final String type;

        public ParamDefNode(Token name, String type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String convertToJott() {
            return name.getToken() + ":" + type;
        }

        @Override public String convertToJava(String className) { throw new UnsupportedOperationException("Not implemented: convertToJava"); }
        @Override public String convertToC() { throw new UnsupportedOperationException("Not implemented: convertToC"); }
        @Override public String convertToPython() { throw new UnsupportedOperationException("Not implemented: convertToPython"); }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents the body of a function definition.
     * Contains variable declarations followed by the main function body (statements and return).
     * Manages the scope and structure within function braces.
     */
    public static class FBodyNode implements JottTree {
        public final List<VarDecNode> varDecs;
        public final BodyNode body;

        public FBodyNode(List<VarDecNode> varDecs, BodyNode body) {
            this.varDecs = varDecs;
            this.body = body;
        }

        @Override
        public String convertToJott() {
            StringBuilder sb = new StringBuilder();
            for (VarDecNode vd : varDecs) {
                sb.append(vd.convertToJott());
            }
            sb.append(body.convertToJott());
            return sb.toString();
        }

        @Override public String convertToJava(String className) { throw new UnsupportedOperationException("Not implemented: convertToJava"); }
        @Override public String convertToC() { throw new UnsupportedOperationException("Not implemented: convertToC"); }
        @Override public String convertToPython() { throw new UnsupportedOperationException("Not implemented: convertToPython"); }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a variable declaration statement.
     * Contains the variable type and identifier name.
     * Serializes as: type name;
     */
    public static class VarDecNode implements JottTree {
        public final String type;
        public final Token name;

        public VarDecNode(String type, Token name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String convertToJott() {
            return type + " " + name.getToken() + ";";
        }

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
        @Override public boolean validateTree() { return true; }
    }

    /**
     * Represents a code body containing statements and optional return statement.
     * Used in function bodies, if/else blocks, and while loop blocks.
     * Handles serialization of all contained statements in sequence.
     */
    public static class BodyNode implements JottTree {
        public final List<JottTree> stmts;
        public final StmtNode.ReturnStmtNode returnStmt;

        public BodyNode(List<JottTree> stmts, StmtNode.ReturnStmtNode returnStmt) {
            this.stmts = stmts;
            this.returnStmt = returnStmt;
        }

        @Override
        public String convertToJott() {
            StringBuilder sb = new StringBuilder();
            for (JottTree stmt : stmts) {
                sb.append(stmt.convertToJott());
            }
            if (returnStmt != null) {
                sb.append(returnStmt.convertToJott());
            }
            return sb.toString();
        }

        @Override public String convertToJava(String className) { return ""; }
        @Override public String convertToC() { return ""; }
        @Override public String convertToPython() { return ""; }
        @Override public boolean validateTree() { return true; }
    }
}
