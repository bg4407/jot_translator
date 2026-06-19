package provided;

/**
 * This class is responsible for paring Jott Tokens
 * into a Jott parse tree.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borniel Gope, Jatin Jain
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 2 recursive descent parser for Jott.
 * Builds a parse tree and implements convertToJott().
 */
public class JottParser {

    private static ArrayList<Token> tokens;
    private static int pos;
    private static boolean errorFound;

    public static JottTree parse(ArrayList<Token> tokenList) {
        if (tokenList == null) return null;

        tokens = tokenList;
        pos = 0;
        errorFound = false;

        ProgramNode root = parseProgram();

        if (errorFound) return null;

        if (peek() != null) {
            syntaxError("Unexpected token after program end", peek());
            return null;
        }

        return root;
    }

    private static Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    private static Token consume() {
        return tokens.get(pos++);
    }

    private static Token lastToken() {
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    private static Token expect(String expected) {
        Token t = peek();

        if (t == null) {
            syntaxError("Expected '" + expected + "' but reached end of file", lastToken());
            return null;
        }

        if (!t.getToken().equals(expected)) {
            syntaxError("Expected '" + expected + "' but got '" + t.getToken() + "'", t);
            return null;
        }

        return consume();
    }

    private static Token expectType(TokenType type, String description) {
        Token t = peek();

        if (t == null) {
            syntaxError("Expected " + description + " but reached end of file", lastToken());
            return null;
        }

        if (t.getTokenType() != type) {
            syntaxError("Expected " + description + " but got '" + t.getToken() + "'", t);
            return null;
        }

        return consume();
    }

    private static void syntaxError(String message, Token location) {
        errorFound = true;
        System.err.println("Syntax Error:");
        System.err.println(message);

        if (location != null) {
            System.err.println(location.getFilename() + ":" + location.getLineNum());
        }
    }

    private static boolean isTypeKeyword(Token t) {
        if (t == null) return false;

        String v = t.getToken();
        return v.equals("Double") ||
               v.equals("Integer") ||
               v.equals("String") ||
               v.equals("Boolean");
    }

    private static boolean lookaheadIsId() {
        return pos + 1 < tokens.size()
                && tokens.get(pos + 1).getTokenType() == TokenType.ID_KEYWORD;
    }

    private static String tokenKindName(Token t) {
        if (t == null) return "EOF";

        switch (t.getTokenType()) {
            case NUMBER:
                return "number";
            case STRING:
                return "string";
            case FC_HEADER:
                return "'::'";
            default:
                return "'" + t.getToken() + "'";
        }
    }

    // program -> function_def*
    private static ProgramNode parseProgram() {
        List<FunctionDefNode> funcs = new ArrayList<>();

        while (peek() != null && !errorFound) {
            if (!peek().getToken().equals("Def")) {
                syntaxError("Expected 'Def' to start a function definition but got '" +
                        peek().getToken() + "'", peek());
                return null;
            }

            FunctionDefNode f = parseFunctionDef();
            if (errorFound) return null;

            funcs.add(f);
        }

        return new ProgramNode(funcs);
    }

    // function_def -> Def id [ func_def_params ] : function_return { f_body }
    private static FunctionDefNode parseFunctionDef() {
        consume(); // Def

        Token nameTok = peek();

        if (nameTok == null) {
            syntaxError("Expected function name but reached end of file", lastToken());
            return null;
        }

        if (nameTok.getTokenType() != TokenType.ID_KEYWORD) {
            syntaxError("Expected id but got " + tokenKindName(nameTok) + " for function name", nameTok);
            return null;
        }

        consume();

        if (expect("[") == null) return null;

        List<ParamDefNode> params = parseFuncDefParams();
        if (errorFound) return null;

        if (expect("]") == null) return null;
        if (expect(":") == null) return null;

        String returnType = parseFunctionReturn();
        if (errorFound) return null;

        if (expect("{") == null) return null;

        FBodyNode fBody = parseFBody();
        if (errorFound) return null;

        if (expect("}") == null) return null;

        return new FunctionDefNode(nameTok, params, returnType, fBody);
    }

    // func_def_params -> id : type (, id : type)* | epsilon
    private static List<ParamDefNode> parseFuncDefParams() {
        List<ParamDefNode> params = new ArrayList<>();

        if (peek() == null || peek().getToken().equals("]")) {
            return params;
        }

        ParamDefNode first = parseOneParamDef();
        if (errorFound) return null;

        params.add(first);

        while (peek() != null && peek().getTokenType() == TokenType.COMMA && !errorFound) {
            consume();

            ParamDefNode next = parseOneParamDef();
            if (errorFound) return null;

            params.add(next);
        }

        return params;
    }

    private static ParamDefNode parseOneParamDef() {
        Token name = expectType(TokenType.ID_KEYWORD, "parameter name");
        if (errorFound) return null;

        if (expect(":") == null) return null;

        String type = parseType();
        if (errorFound) return null;

        return new ParamDefNode(name, type);
    }

    // type -> Double | Integer | String | Boolean
    private static String parseType() {
        Token t = peek();

        if (t == null) {
            syntaxError("Expected type but reached end of file", lastToken());
            return null;
        }

        String v = t.getToken();

        if (v.equals("Double") || v.equals("Integer") || v.equals("String") || v.equals("Boolean")) {
            consume();
            return v;
        }

        syntaxError("Expected type (Double/Integer/String/Boolean) but got '" + v + "'", t);
        return null;
    }

    // function_return -> type | Void
    private static String parseFunctionReturn() {
        Token t = peek();

        if (t != null && t.getToken().equals("Void")) {
            consume();
            return "Void";
        }

        return parseType();
    }

    // f_body -> var_dec* body
    private static FBodyNode parseFBody() {
        List<VarDecNode> varDecs = new ArrayList<>();

        while (!errorFound && peek() != null && isTypeKeyword(peek()) && lookaheadIsId()) {
            VarDecNode vd = parseVarDec();
            if (errorFound) return null;

            varDecs.add(vd);
        }

        BodyNode body = parseBody();
        if (errorFound) return null;

        return new FBodyNode(varDecs, body);
    }

    // var_dec -> type id ;
    private static VarDecNode parseVarDec() {
        String type = parseType();
        if (errorFound) return null;

        Token name = expectType(TokenType.ID_KEYWORD, "variable name");
        if (errorFound) return null;

        if (expect(";") == null) return null;

        return new VarDecNode(type, name);
    }

    // body -> body_stmt* return_stmt?
    private static BodyNode parseBody() {
        List<JottTree> stmts = new ArrayList<>();

        while (!errorFound && peek() != null && isBodyStmtStart()) {
            JottTree stmt = parseBodyStmt();
            if (errorFound) return null;

            stmts.add(stmt);
        }

        ReturnStmtNode ret = null;

        if (!errorFound && peek() != null && peek().getToken().equals("Return")) {
            ret = parseReturnStmt();
            if (errorFound) return null;
        }

        return new BodyNode(stmts, ret);
    }

    private static boolean isBodyStmtStart() {
        Token t = peek();
        if (t == null) return false;

        if (t.getTokenType() == TokenType.FC_HEADER) return true;
        if (t.getToken().equals("If")) return true;
        if (t.getToken().equals("While")) return true;

        if (t.getTokenType() == TokenType.ID_KEYWORD) {
            String v = t.getToken();

            return !v.equals("Return")
                    && !v.equals("Else")
                    && !v.equals("Elseif")
                    && !v.equals("Def");
        }

        return false;
    }

    // body_stmt -> if_stmt | while_loop | asmt | func_call ;
    private static JottTree parseBodyStmt() {
        Token t = peek();

        if (t.getToken().equals("If")) {
            return parseIfStmt();
        }

        if (t.getToken().equals("While")) {
            return parseWhileLoop();
        }

        if (t.getTokenType() == TokenType.FC_HEADER) {
            FuncCallNode fc = parseFuncCall();
            if (errorFound) return null;

            if (expect(";") == null) return null;

            return fc;
        }

        return parseAsmt();
    }

    // return_stmt -> Return expr ;
    private static ReturnStmtNode parseReturnStmt() {
        Token retTok = consume();

        ExprNode expr = parseExpr();

        if (expr == null && !errorFound) {
            syntaxError("Expected expression after 'Return'", peek());
            return null;
        }

        if (errorFound) return null;

        if (expect(";") == null) return null;

        return new ReturnStmtNode(retTok, expr);
    }

    // asmt -> id = expr ;
    private static AsmtNode parseAsmt() {
        Token id = expectType(TokenType.ID_KEYWORD, "variable name for assignment");
        if (errorFound) return null;

        Token eq = peek();

        if (eq == null || eq.getTokenType() != TokenType.ASSIGN) {
            syntaxError("Expected '=' for assignment but got '" +
                    (eq == null ? "EOF" : eq.getToken()) + "'", eq);
            return null;
        }

        consume();

        ExprNode expr = parseExpr();

        if (expr == null && !errorFound) {
            syntaxError("Assignment missing right side expression", peek());
            return null;
        }

        if (errorFound) return null;

        if (expect(";") == null) return null;

        return new AsmtNode(id, expr);
    }

    // if_stmt -> If [ expr ] { body } elseif* else?
    private static IfStmtNode parseIfStmt() {
        Token ifTok = consume();

        if (expect("[") == null) return null;

        ExprNode cond = parseExpr();

        if (cond == null && !errorFound) {
            syntaxError("Expected condition expression in If statement", peek());
            return null;
        }

        if (errorFound) return null;

        if (expect("]") == null) return null;
        if (expect("{") == null) return null;

        BodyNode body = parseBody();
        if (errorFound) return null;

        if (expect("}") == null) return null;

        List<ElseIfNode> elseIfs = new ArrayList<>();

        while (!errorFound && peek() != null && peek().getToken().equals("Elseif")) {
            elseIfs.add(parseElseIf());
            if (errorFound) return null;
        }

        ElseNode elseNode = null;

        if (!errorFound && peek() != null && peek().getToken().equals("Else")) {
            elseNode = parseElse();
            if (errorFound) return null;
        }

        return new IfStmtNode(ifTok, cond, body, elseIfs, elseNode);
    }

    // elseif -> Elseif [ expr ] { body }
    private static ElseIfNode parseElseIf() {
        Token tok = consume();

        if (expect("[") == null) return null;

        ExprNode cond = parseExpr();

        if (cond == null && !errorFound) {
            syntaxError("Expected condition expression in Elseif statement", peek());
            return null;
        }

        if (errorFound) return null;

        if (expect("]") == null) return null;
        if (expect("{") == null) return null;

        BodyNode body = parseBody();
        if (errorFound) return null;

        if (expect("}") == null) return null;

        return new ElseIfNode(tok, cond, body);
    }

    // else -> Else { body }
    private static ElseNode parseElse() {
        Token tok = consume();

        if (expect("{") == null) return null;

        BodyNode body = parseBody();
        if (errorFound) return null;

        if (expect("}") == null) return null;

        return new ElseNode(tok, body);
    }

    // while_loop -> While [ expr ] { body }
    private static WhileNode parseWhileLoop() {
        Token tok = consume();

        if (expect("[") == null) return null;

        ExprNode cond = parseExpr();

        if (cond == null && !errorFound) {
            syntaxError("Expected condition expression in While statement", peek());
            return null;
        }

        if (errorFound) return null;

        if (expect("]") == null) return null;
        if (expect("{") == null) return null;

        BodyNode body = parseBody();
        if (errorFound) return null;

        if (expect("}") == null) return null;

        return new WhileNode(tok, cond, body);
    }

    // func_call -> :: id [ params ]
    private static FuncCallNode parseFuncCall() {
        Token header = expectType(TokenType.FC_HEADER, "'::'");
        if (errorFound) return null;

        Token name = expectType(TokenType.ID_KEYWORD, "function name");
        if (errorFound) return null;

        if (expect("[") == null) return null;

        List<ExprNode> params = parseParams();
        if (errorFound) return null;

        if (expect("]") == null) return null;

        return new FuncCallNode(header, name, params);
    }

    // params -> expr (, expr)* | epsilon
    private static List<ExprNode> parseParams() {
        List<ExprNode> params = new ArrayList<>();

        if (peek() == null || peek().getToken().equals("]")) {
            return params;
        }

        ExprNode first = parseExpr();

        if (first == null && !errorFound) {
            syntaxError("Expected expression in function call parameters", peek());
            return null;
        }

        if (errorFound) return null;

        params.add(first);

        while (!errorFound && peek() != null && peek().getTokenType() == TokenType.COMMA) {
            consume();

            ExprNode next = parseExpr();

            if (next == null && !errorFound) {
                syntaxError("Expected expression after ',' in function call parameters", peek());
                return null;
            }

            if (errorFound) return null;

            params.add(next);
        }

        return params;
    }

    // expr -> operand | operand relop operand | operand mathop operand | string_literal | bool
    private static ExprNode parseExpr() {
        Token t = peek();

        if (t == null) {
            syntaxError("Expected expression but reached end of file", lastToken());
            return null;
        }

        TokenType tt = t.getTokenType();

        if (tt == TokenType.SEMICOLON || tt == TokenType.R_BRACKET || tt == TokenType.R_BRACE) {
            return null;
        }

        if (tt == TokenType.STRING) {
            consume();
            return new ExprNode(new StringLiteralNode(t), null, null);
        }

        if (t.getToken().equals("True") || t.getToken().equals("False")) {
            consume();
            return new ExprNode(new BoolNode(t), null, null);
        }

        OperandNode left = parseOperand();
        if (errorFound) return null;

        Token op = peek();

        if (op != null &&
                (op.getTokenType() == TokenType.REL_OP || op.getTokenType() == TokenType.MATH_OP)) {
            consume();

            OperandNode right = parseOperand();
            if (errorFound) return null;

            return new ExprNode(left, op, right);
        }

        return new ExprNode(left, null, null);
    }

    // operand -> id | num | func_call | -num
    private static OperandNode parseOperand() {
        Token t = peek();

        if (t == null) {
            syntaxError("Expected operand but reached end of file", lastToken());
            return null;
        }

        if (t.getTokenType() == TokenType.MATH_OP && t.getToken().equals("-")) {
            Token minus = consume();
            Token num = peek();

            if (num == null || num.getTokenType() != TokenType.NUMBER) {
                syntaxError("Expected number after unary '-'", minus);
                return null;
            }

            consume();
            return new OperandNode(minus, num);
        }

        if (t.getTokenType() == TokenType.NUMBER) {
            consume();
            return new OperandNode(null, t);
        }

        if (t.getTokenType() == TokenType.FC_HEADER) {
            FuncCallNode fc = parseFuncCall();
            if (errorFound) return null;

            return new OperandNode(fc);
        }

        if (t.getTokenType() == TokenType.ID_KEYWORD) {
            consume();
            return new OperandNode(t);
        }

        syntaxError("Expected operand but got '" + t.getToken() + "'", t);
        return null;
    }

    public static class ProgramNode implements JottTree {
        public final List<FunctionDefNode> funcs;

        ProgramNode(List<FunctionDefNode> funcs) {
            this.funcs = funcs;
        }

        public String convertToJott() {
            StringBuilder sb = new StringBuilder();

            for (FunctionDefNode f : funcs) {
                sb.append(f.convertToJott());
            }

            return sb.toString();
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class FunctionDefNode implements JottTree {
        public final Token name;
        public final List<ParamDefNode> params;
        public final String returnType;
        public final FBodyNode fBody;

        FunctionDefNode(Token name, List<ParamDefNode> params, String returnType, FBodyNode fBody) {
            this.name = name;
            this.params = params;
            this.returnType = returnType;
            this.fBody = fBody;
        }

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

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class ParamDefNode implements JottTree {
        public final Token name;
        public final String type;

        ParamDefNode(Token name, String type) {
            this.name = name;
            this.type = type;
        }

        public String convertToJott() {
            return name.getToken() + ":" + type;
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class FBodyNode implements JottTree {
        public final List<VarDecNode> varDecs;
        public final BodyNode body;

        FBodyNode(List<VarDecNode> varDecs, BodyNode body) {
            this.varDecs = varDecs;
            this.body = body;
        }

        public String convertToJott() {
            StringBuilder sb = new StringBuilder();

            for (VarDecNode vd : varDecs) {
                sb.append(vd.convertToJott());
            }

            sb.append(body.convertToJott());

            return sb.toString();
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class VarDecNode implements JottTree {
        public final String type;
        public final Token name;

        VarDecNode(String type, Token name) {
            this.type = type;
            this.name = name;
        }

        public String convertToJott() {
            return type + " " + name.getToken() + ";";
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class BodyNode implements JottTree {
        public final List<JottTree> stmts;
        public final ReturnStmtNode returnStmt;

        BodyNode(List<JottTree> stmts, ReturnStmtNode returnStmt) {
            this.stmts = stmts;
            this.returnStmt = returnStmt;
        }

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

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class ReturnStmtNode implements JottTree {
        public final Token returnToken;
        public final ExprNode expr;

        ReturnStmtNode(Token returnToken, ExprNode expr) {
            this.returnToken = returnToken;
            this.expr = expr;
        }

        public String convertToJott() {
            return "Return " + expr.convertToJott() + ";";
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class AsmtNode implements JottTree {
        public final Token id;
        public final ExprNode expr;

        AsmtNode(Token id, ExprNode expr) {
            this.id = id;
            this.expr = expr;
        }

        public String convertToJott() {
            return id.getToken() + "=" + expr.convertToJott() + ";";
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class IfStmtNode implements JottTree {
        public final Token ifToken;
        public final ExprNode cond;
        public final BodyNode body;
        public final List<ElseIfNode> elseIfs;
        public final ElseNode elseNode;

        IfStmtNode(Token ifToken, ExprNode cond, BodyNode body,
                   List<ElseIfNode> elseIfs, ElseNode elseNode) {
            this.ifToken = ifToken;
            this.cond = cond;
            this.body = body;
            this.elseIfs = elseIfs;
            this.elseNode = elseNode;
        }

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

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class ElseIfNode implements JottTree {
        public final Token elseifToken;
        public final ExprNode cond;
        public final BodyNode body;

        ElseIfNode(Token elseifToken, ExprNode cond, BodyNode body) {
            this.elseifToken = elseifToken;
            this.cond = cond;
            this.body = body;
        }

        public String convertToJott() {
            return "Elseif[" + cond.convertToJott() + "]{" + body.convertToJott() + "}";
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class ElseNode implements JottTree {
        public final Token elseToken;
        public final BodyNode body;

        ElseNode(Token elseToken, BodyNode body) {
            this.elseToken = elseToken;
            this.body = body;
        }

        public String convertToJott() {
            return "Else{" + body.convertToJott() + "}";
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class WhileNode implements JottTree {
        public final Token whileToken;
        public final ExprNode cond;
        public final BodyNode body;

        WhileNode(Token whileToken, ExprNode cond, BodyNode body) {
            this.whileToken = whileToken;
            this.cond = cond;
            this.body = body;
        }

        public String convertToJott() {
            return "While[" + cond.convertToJott() + "]{" + body.convertToJott() + "}";
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class FuncCallNode implements JottTree {
        public final Token header;
        public final Token name;
        public final List<ExprNode> params;

        FuncCallNode(Token header, Token name, List<ExprNode> params) {
            this.header = header;
            this.name = name;
            this.params = params;
        }

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

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class ExprNode implements JottTree {
        public final JottTree left;
        public final Token op;
        public final OperandNode right;

        ExprNode(JottTree left, Token op, OperandNode right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String convertToJott() {
            if (op == null) {
                return left.convertToJott();
            }

            return left.convertToJott() + op.getToken() + right.convertToJott();
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class OperandNode implements JottTree {
        public final Token idTok;
        public final Token minusTok;
        public final Token numTok;
        public final FuncCallNode funcCall;

        OperandNode(Token idTok) {
            this.idTok = idTok;
            this.minusTok = null;
            this.numTok = null;
            this.funcCall = null;
        }

        OperandNode(Token minusTok, Token numTok) {
            this.idTok = null;
            this.minusTok = minusTok;
            this.numTok = numTok;
            this.funcCall = null;
        }

        OperandNode(FuncCallNode funcCall) {
            this.idTok = null;
            this.minusTok = null;
            this.numTok = null;
            this.funcCall = funcCall;
        }

        public String convertToJott() {
            if (funcCall != null) {
                return funcCall.convertToJott();
            }

            if (numTok != null) {
                return (minusTok != null ? "-" : "") + numTok.getToken();
            }

            return idTok.getToken();
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class StringLiteralNode implements JottTree {
        public final Token tok;

        StringLiteralNode(Token tok) {
            this.tok = tok;
        }

        public String convertToJott() {
            return tok.getToken();
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }

    public static class BoolNode implements JottTree {
        public final Token tok;

        BoolNode(Token tok) {
            this.tok = tok;
        }

        public String convertToJott() {
            return tok.getToken();
        }

        public String convertToJava(String className) { return ""; }
        public String convertToC() { return ""; }
        public String convertToPython() { return ""; }
        public boolean validateTree() { return true; }
    }
}

/**
 *  things to Implement AST Node classes 
 * ProgramNode
 * FunctionDefNode
 * ParamDefNode
 * FBodyNode
 * VarDecNode
 * BodyNode
 * ReturnStmtNode
 * AsmtNode
 * IfStmtNode
 * ElseIfNode
 * ElseNode
 * WhileNode
 * FuncCallNode
 * ExprNode
 * OperandNode
 * StringLiteralNode
 * BoolNode
 */