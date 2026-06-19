package provided;

/**
 * This class is responsible for paring Jott Tokens
 * into a Jott parse tree.
 *
 * @author Conner Meagher, Anindita Bhowmik, Borniel Gope, Jatin Jain
 */

import java.util.ArrayList;
import java.util.List;
import provided.ProgramStructureNode.*;
import provided.StmtNode.*;
import provided.ControlFlowNode.*;
import provided.ExpressionNode.*;

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

            return new FuncCallStmtNode(fc);
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

}