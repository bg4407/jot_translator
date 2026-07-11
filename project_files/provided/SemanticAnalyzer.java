package provided;

/**
 * Phase 3 semantic analyzer for Jott.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borneil Gope, Jatin Jain
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import provided.ProgramStructureNode.*;
import provided.StmtNode.*;
import provided.ControlFlowNode.*;
import provided.ExpressionNode.*;

public class SemanticAnalyzer {

    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
            "Def", "Return", "If", "Elseif", "Else", "While", "Void",
            "Double", "Integer", "String", "Boolean"));

    private static final Set<String> BUILTIN_NAMES = new HashSet<>(Arrays.asList(
            "print", "concat", "length"));

    private static final Map<String, FunctionSignature> BUILTIN_SIGNATURES = createBuiltinSignatures();

    private boolean errorFound = false;

    public boolean validateProgram(ProgramStructureNode.ProgramNode program) {
        Map<String, FunctionSignature> env = new LinkedHashMap<>();
        env.putAll(BUILTIN_SIGNATURES);

        Set<String> seenFunctions = new HashSet<>();
        FunctionSignature mainSig = null;

        for (FunctionDefNode func : program.funcs) {
            String name = func.name.getToken();

            if (BUILTIN_NAMES.contains(name)) {
                semanticError("Cannot define builtin function '" + name + "'", func.name);
            }

            if (RESERVED_KEYWORDS.contains(name)) {
                semanticError("Function name '" + name + "' is a reserved keyword", func.name);
            }

            if (seenFunctions.contains(name)) {
                semanticError("Duplicate function name '" + name + "'", func.name);
            }

            seenFunctions.add(name);
            FunctionSignature sig = functionSignatureFromDef(func);

            if (name.equals("main")) {
                mainSig = sig;
            }

            validateFunction(func, env);
            env.put(sig.name, sig);
        }

        if (mainSig == null) {
            Token location = program.funcs.isEmpty() ? null : program.funcs.get(0).name;
            semanticError("Missing main function", location);
        } else {
            if (!mainSig.returnType.equals("Void")) {
                semanticError("Main function must have return type Void", mainSig.token);
            }
            if (!mainSig.paramTypes.isEmpty()) {
                semanticError("Main function must have no parameters", mainSig.token);
            }
        }

        return !errorFound;
    }

    private void validateFunction(FunctionDefNode func, Map<String, FunctionSignature> env) {
        Map<String, String> variableTypes = new HashMap<>();

        for (ParamDefNode param : func.params) {
            String name = param.name.getToken();
            if (RESERVED_KEYWORDS.contains(name)) {
                semanticError("Identifier '" + name + "' is a reserved keyword", param.name);
            }
            if (variableTypes.containsKey(name)) {
                semanticError("Duplicate parameter name '" + name + "'", param.name);
            } else {
                variableTypes.put(name, param.type);
            }
        }

        for (ProgramStructureNode.VarDecNode varDec : func.fBody.varDecs) {
            String name = varDec.name.getToken();
            if (RESERVED_KEYWORDS.contains(name)) {
                semanticError("Identifier '" + name + "' is a reserved keyword", varDec.name);
            }
            if (variableTypes.containsKey(name)) {
                semanticError("Duplicate variable name '" + name + "'", varDec.name);
            } else {
                variableTypes.put(name, varDec.type);
            }
        }

        boolean bodyReturns = validateBody(func.fBody.body, env, variableTypes, func.returnType, func.name.getToken());

        if (!func.returnType.equals("Void") && !bodyReturns) {
            semanticError("Function '" + func.name.getToken() + "' may not return a value on all paths", func.name);
        }
    }

    private boolean validateBody(ProgramStructureNode.BodyNode body,
                                 Map<String, FunctionSignature> env,
                                 Map<String, String> variableTypes,
                                 String functionReturnType,
                                 String functionName) {
        boolean hasReturnPath = false;

        for (JottTree stmt : body.stmts) {
            boolean stmtReturns = validateStatement(stmt, env, variableTypes, functionReturnType, functionName);
            if (!hasReturnPath && stmtReturns) {
                hasReturnPath = true;
            }
        }

        if (body.returnStmt != null) {
            validateReturnStmt(body.returnStmt, env, variableTypes, functionReturnType, functionName);
            hasReturnPath = true;
        }

        return hasReturnPath;
    }

    private boolean validateStatement(JottTree stmt,
                                      Map<String, FunctionSignature> env,
                                      Map<String, String> variableTypes,
                                      String functionReturnType,
                                      String functionName) {
        if (stmt instanceof StmtNode.ReturnStmtNode) {
            validateReturnStmt((StmtNode.ReturnStmtNode) stmt, env, variableTypes, functionReturnType, functionName);
            return true;
        }

        if (stmt instanceof StmtNode.AsmtNode) {
            validateAssignment((StmtNode.AsmtNode) stmt, env, variableTypes, functionName);
            return false;
        }

        if (stmt instanceof StmtNode.FuncCallStmtNode) {
            validateFuncCall(((StmtNode.FuncCallStmtNode) stmt).funcCall, env, variableTypes, functionName);
            return false;
        }

        if (stmt instanceof ControlFlowNode.IfStmtNode) {
            return validateIfStmt((ControlFlowNode.IfStmtNode) stmt, env, variableTypes, functionReturnType, functionName);
        }

        if (stmt instanceof ControlFlowNode.WhileNode) {
            validateWhileNode((ControlFlowNode.WhileNode) stmt, env, variableTypes, functionReturnType, functionName);
            return false;
        }

        return false;
    }

    private void validateAssignment(StmtNode.AsmtNode asmt,
                                    Map<String, FunctionSignature> env,
                                    Map<String, String> variableTypes,
                                    String functionName) {
        String varName = asmt.id.getToken();
        if (!variableTypes.containsKey(varName)) {
            semanticError("Variable '" + varName + "' is not declared", asmt.id);
            return;
        }

        String expectedType = variableTypes.get(varName);
        String valueType = validateExpr(asmt.expr, env, variableTypes, functionName);
        if (valueType == null) return;

        if (!typesCompatible(expectedType, valueType)) {
            semanticError("Cannot assign value of type '" + valueType + "' to variable '" + varName + "' of type '" + expectedType + "'", asmt.id);
        }
    }

    private void validateFuncCall(ExpressionNode.FuncCallNode funcCall,
                                  Map<String, FunctionSignature> env,
                                  Map<String, String> variableTypes,
                                  String functionName) {
        FunctionSignature signature = env.get(funcCall.name.getToken());
        if (signature == null) {
            semanticError("Undefined function '" + funcCall.name.getToken() + "'", funcCall.name);
            return;
        }

        validateFunctionCallArguments(funcCall, signature, env, variableTypes, functionName);
    }

    private void validateFunctionCallArguments(ExpressionNode.FuncCallNode funcCall,
                                               FunctionSignature signature,
                                               Map<String, FunctionSignature> env,
                                               Map<String, String> variableTypes,
                                               String functionName) {
        int expected = signature.paramTypes.size();
        int found = funcCall.params.size();

        if (expected != found) {
            semanticError("Function '" + signature.name + "' expects " + expected + " arguments but got " + found, funcCall.name);
            return;
        }

        for (int i = 0; i < expected; i++) {
            String expectedType = signature.paramTypes.get(i);
            String actualType = validateExpr(funcCall.params.get(i), env, variableTypes, functionName);
            if (actualType == null) continue;

            if (expectedType.equals("Any")) {
                if (actualType.equals("Void")) {
                    semanticError("Function '" + signature.name + "' cannot accept Void as an argument", funcCall.name);
                }
                continue;
            }

            if (!typesCompatible(expectedType, actualType)) {
                semanticError("Function '" + signature.name + "' expects argument " + (i + 1) + " of type '" + expectedType + "' but got '" + actualType + "'", funcCall.name);
            }
        }
    }

    private boolean validateIfStmt(ControlFlowNode.IfStmtNode ifNode,
                                   Map<String, FunctionSignature> env,
                                   Map<String, String> variableTypes,
                                   String functionReturnType,
                                   String functionName) {
        String condType = validateExpr(ifNode.cond, env, variableTypes, functionName);
        if (condType != null && !condType.equals("Boolean")) {
            semanticError("Condition expression must be Boolean", ifNode.cond == null ? ifNode.ifToken : getExprToken(ifNode.cond));
        }

        boolean ifReturns = validateBody(ifNode.body, env, variableTypes, functionReturnType, functionName);

        boolean allElseIfReturn = true;
        for (ControlFlowNode.ElseIfNode elseIf : ifNode.elseIfs) {
            String elseIfType = validateExpr(elseIf.cond, env, variableTypes, functionName);
            if (elseIfType != null && !elseIfType.equals("Boolean")) {
                semanticError("Condition expression must be Boolean", getExprToken(elseIf.cond));
            }
            boolean elseifReturns = validateBody(elseIf.body, env, variableTypes, functionReturnType, functionName);
            if (!elseifReturns) {
                allElseIfReturn = false;
            }
        }

        boolean elseReturns = false;
        if (ifNode.elseNode != null) {
            elseReturns = validateBody(ifNode.elseNode.body, env, variableTypes, functionReturnType, functionName);
        }

        return ifNode.elseNode != null && ifReturns && allElseIfReturn && elseReturns;
    }

    private void validateWhileNode(ControlFlowNode.WhileNode whileNode,
                                   Map<String, FunctionSignature> env,
                                   Map<String, String> variableTypes,
                                   String functionReturnType,
                                   String functionName) {
        String condType = validateExpr(whileNode.cond, env, variableTypes, functionName);
        if (condType != null && !condType.equals("Boolean")) {
            semanticError("Condition expression must be Boolean", getExprToken(whileNode.cond));
        }

        // Note: intentionally do not propagate the body's return-guarantee
        // here. A while loop's body may never execute, so a Return inside it
        // can never satisfy the enclosing function's return requirement.
        validateBody(whileNode.body, env, variableTypes, functionReturnType, functionName);
    }

    private void validateReturnStmt(StmtNode.ReturnStmtNode returnStmt,
                                    Map<String, FunctionSignature> env,
                                    Map<String, String> variableTypes,
                                    String functionReturnType,
                                    String functionName) {
        String valueType = validateExpr(returnStmt.expr, env, variableTypes, functionName);
        if (valueType == null) return;

        if (functionReturnType.equals("Void")) {
            semanticError("Void function '" + functionName + "' cannot return a value", returnStmt.returnToken);
            return;
        }

        if (!typesCompatible(functionReturnType, valueType)) {
            semanticError("Function '" + functionName + "' must return a value of type '" + functionReturnType + "' but got '" + valueType + "'", returnStmt.returnToken);
        }
    }

    private String validateExpr(ExpressionNode.ExprNode expr,
                                Map<String, FunctionSignature> env,
                                Map<String, String> variableTypes,
                                String functionName) {
        if (expr == null) {
            return null;
        }

        if (expr.op == null) {
            return validateSimpleExpr(expr.left, env, variableTypes, functionName);
        }

        String leftType = validateSimpleExpr(expr.left, env, variableTypes, functionName);
        String rightType = validateOperand(expr.right, env, variableTypes, functionName);
        if (leftType == null || rightType == null) {
            return null;
        }

        if (expr.op.getTokenType() == TokenType.MATH_OP) {
            if (!isNumeric(leftType) || !isNumeric(rightType)) {
                semanticError("Math operator '" + expr.op.getToken() + "' requires numeric operands", expr.op);
                return null;
            }

            // Jott does not allow mixing Integer and Double operands, even
            // though both are numeric - the spec requires an exact type match.
            if (!leftType.equals(rightType)) {
                semanticError("Cannot apply '" + expr.op.getToken() + "' to mismatched numeric types '" +
                        leftType + "' and '" + rightType + "'", expr.op);
                return null;
            }

            if (expr.op.getToken().equals("/") && isZeroLiteral(expr.right)) {
                semanticError("Division by zero", expr.op);
                return null;
            }

            return leftType;
        }

        if (expr.op.getTokenType() == TokenType.REL_OP) {
            String op = expr.op.getToken();

            boolean orderingOp = op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=");
            boolean equalityOp = op.equals("==") || op.equals("!=");

            if (orderingOp) {
                if (!isNumeric(leftType) || !isNumeric(rightType)) {
                    semanticError("Relational operator '" + op + "' requires numeric operands", expr.op);
                    return null;
                }
                return "Boolean";
            }

            if (equalityOp) {
                if (!leftType.equals(rightType) && !isNumeric(leftType, rightType)) {
                    semanticError("Relational operator '" + op + "' requires operands of the same type", expr.op);
                    return null;
                }
                return "Boolean";
            }

            semanticError("Relational operator '" + op + "' is not supported", expr.op);
            return null;
        }

        semanticError("Unknown operator '" + expr.op.getToken() + "'", expr.op);
        return null;
    }

    private String validateSimpleExpr(JottTree left,
                                      Map<String, FunctionSignature> env,
                                      Map<String, String> variableTypes,
                                      String functionName) {
        if (left instanceof ExpressionNode.StringLiteralNode) {
            return "String";
        }

        if (left instanceof ExpressionNode.BoolNode) {
            return "Boolean";
        }

        if (left instanceof ExpressionNode.OperandNode) {
            return validateOperand((ExpressionNode.OperandNode) left, env, variableTypes, functionName);
        }

        semanticError("Invalid expression", extractToken(left));
        return null;
    }

    private String validateOperand(ExpressionNode.OperandNode operand,
                                   Map<String, FunctionSignature> env,
                                   Map<String, String> variableTypes,
                                   String functionName) {
        if (operand == null) {
            return null;
        }

        if (operand.funcCall != null) {
            return validateFunctionCallExpression(operand.funcCall, env, variableTypes, functionName);
        }

        if (operand.numTok != null) {
            return operand.numTok.getToken().contains(".") ? "Double" : "Integer";
        }

        if (operand.idTok != null) {
            String name = operand.idTok.getToken();
            if (!variableTypes.containsKey(name)) {
                semanticError("Variable '" + name + "' is not declared", operand.idTok);
                return null;
            }
            return variableTypes.get(name);
        }

        semanticError("Invalid operand", extractToken(operand));
        return null;
    }

    private String validateFunctionCallExpression(ExpressionNode.FuncCallNode funcCall,
                                                  Map<String, FunctionSignature> env,
                                                  Map<String, String> variableTypes,
                                                  String functionName) {
        FunctionSignature signature = env.get(funcCall.name.getToken());
        if (signature == null) {
            semanticError("Undefined function '" + funcCall.name.getToken() + "'", funcCall.name);
            return null;
        }

        validateFunctionCallArguments(funcCall, signature, env, variableTypes, functionName);
        return signature.returnType;
    }

    private static boolean isNumeric(String type) {
        return type.equals("Integer") || type.equals("Double");
    }

    private static boolean isNumeric(String leftType, String rightType) {
        return isNumeric(leftType) && isNumeric(rightType);
    }

    private static boolean typesCompatible(String expected, String actual) {
        return expected.equals(actual);
    }

    /** True if this operand is a numeric literal equal to zero (e.g. 0 or 0.0). Sign is irrelevant. */
    private static boolean isZeroLiteral(ExpressionNode.OperandNode operand) {
        if (operand == null || operand.numTok == null) {
            return false;
        }
        try {
            return Double.parseDouble(operand.numTok.getToken()) == 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Token extractToken(JottTree node) {
        if (node instanceof ExpressionNode.OperandNode) {
            ExpressionNode.OperandNode op = (ExpressionNode.OperandNode) node;
            return op.idTok != null ? op.idTok : op.numTok != null ? op.numTok : op.funcCall.header;
        }
        if (node instanceof ExpressionNode.ExprNode) {
            ExpressionNode.ExprNode expr = (ExpressionNode.ExprNode) node;
            if (expr.left instanceof ExpressionNode.OperandNode) {
                return extractToken(expr.left);
            }
            if (expr.left instanceof ExpressionNode.StringLiteralNode) {
                return ((ExpressionNode.StringLiteralNode) expr.left).tok;
            }
            if (expr.left instanceof ExpressionNode.BoolNode) {
                return ((ExpressionNode.BoolNode) expr.left).tok;
            }
        }
        if (node instanceof StmtNode.AsmtNode) {
            return ((StmtNode.AsmtNode) node).id;
        }
        if (node instanceof StmtNode.ReturnStmtNode) {
            return ((StmtNode.ReturnStmtNode) node).returnToken;
        }
        return null;
    }

    private Token getExprToken(ExpressionNode.ExprNode expr) {
        if (expr.left instanceof ExpressionNode.OperandNode) {
            return extractToken((ExpressionNode.OperandNode) expr.left);
        }
        if (expr.left instanceof ExpressionNode.StringLiteralNode) {
            return ((ExpressionNode.StringLiteralNode) expr.left).tok;
        }
        if (expr.left instanceof ExpressionNode.BoolNode) {
            return ((ExpressionNode.BoolNode) expr.left).tok;
        }
        return null;
    }

    private void semanticError(String message, Token location) {
        errorFound = true;
        System.err.println("Semantic Error:");
        System.err.println(message);
        if (location != null) {
            System.err.println(location.getFilename() + ":" + location.getLineNum());
        }
    }

    private static Map<String, FunctionSignature> createBuiltinSignatures() {
        Map<String, FunctionSignature> builtins = new LinkedHashMap<>();
        builtins.put("print", new FunctionSignature("print", "Void", Arrays.asList("Any"), null));
        builtins.put("concat", new FunctionSignature("concat", "String", Arrays.asList("String", "String"), null));
        builtins.put("length", new FunctionSignature("length", "Integer", Arrays.asList("String"), null));
        return builtins;
    }

    private FunctionSignature functionSignatureFromDef(FunctionDefNode func) {
        List<String> paramTypes = new ArrayList<>();
        for (ParamDefNode param : func.params) {
            paramTypes.add(param.type);
        }
        return new FunctionSignature(func.name.getToken(), func.returnType, paramTypes, func.name);
    }

    private static class FunctionSignature {
        private final String name;
        private final String returnType;
        private final List<String> paramTypes;
        private final Token token;

        public FunctionSignature(String name, String returnType, List<String> paramTypes, Token token) {
            this.name = name;
            this.returnType = returnType;
            this.paramTypes = paramTypes;
            this.token = token;
        }
    }

}