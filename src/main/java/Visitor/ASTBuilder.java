package Visitor;

import ast.*;
import parser.MiniCppBaseVisitor;
import parser.MiniCppParser;

public class ASTBuilder extends MiniCppBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitProgram(MiniCppParser.ProgramContext ctx) {
        Program p = new Program();

        for (MiniCppParser.FunctionDeclContext f : ctx.functionDecl()) {
            p.declarations.add((ASTNode) visit(f));
        }
        for (MiniCppParser.ClassDeclContext c : ctx.classDecl()) {
            p.declarations.add((ASTNode) visit(c));
        }
        for (MiniCppParser.StmtContext s : ctx.stmt()) {
            p.declarations.add((ASTNode) visit(s));
        }

        return p;
    }
    // ---------- Statements ----------

    @Override
    public ast.ASTNode visitExprStmt(parser.MiniCppParser.ExprStmtContext ctx) {
        ast.Expr e = (ast.Expr) visit(ctx.e);
        return new ast.ExprStmt(e);
    }

    @Override
    public ast.ASTNode visitBlock(parser.MiniCppParser.BlockContext ctx) {
        ast.BlockStmt b = new ast.BlockStmt();
        for (parser.MiniCppParser.StmtContext s : ctx.stmt()) {
            b.statements.add((ast.Statement) visit(s));
        }
        return b;
    }

    @Override
    public ast.ASTNode visitBlockStmt(parser.MiniCppParser.BlockStmtContext ctx) {
        return visit(ctx.block());
    }

    @Override
    public ast.ASTNode visitVarDecl(parser.MiniCppParser.VarDeclContext ctx) {
        String name = ctx.ID().getText();
        ast.TypeNode type = (ast.TypeNode) visit(ctx.type());
        ast.Expr init = (ctx.expr() != null) ? (ast.Expr) visit(ctx.expr()) : null;
        return new ast.VarDeclStmt(name, type, init);
    }

    @Override
    public ast.ASTNode visitVarDeclStmt(parser.MiniCppParser.VarDeclStmtContext ctx) {
        return visit(ctx.varDecl());
    }


    @Override
    public ASTNode visitIntLiteral(MiniCppParser.IntLiteralContext ctx) {
        return new IntLiteral(Integer.parseInt(ctx.INT().getText()));
    }

    @Override
    public ASTNode visitVar(MiniCppParser.VarContext ctx) {
        return new VarExpr(ctx.ID().getText());
    }

    @Override
    public ASTNode visitParens(MiniCppParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ast.ASTNode visitFunctionDecl(parser.MiniCppParser.FunctionDeclContext ctx) {
        // Funktionsname
        String name = ctx.ID().getText();

        // Parameter (mit Typen!)
        java.util.List<ast.Param> params = new java.util.ArrayList<>();
        if (ctx.paramList() != null) {
            for (parser.MiniCppParser.ParamContext p : ctx.paramList().param()) {
                ast.TypeNode pt = (ast.TypeNode) visit(p.type());
                String pn = p.ID().getText();
                params.add(new ast.Param(pt, pn));
            }
        }

        // Funktionsbody
        ast.BlockStmt body = (ast.BlockStmt) visit(ctx.block());

        // AST-Knoten erzeugen
        return new ast.FunctionDecl(name, params, body);
    }




    // ---------- Expressions ----------

    @Override
    public ast.ASTNode visitAdd(parser.MiniCppParser.AddContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.additiveExpr());
        ast.Expr right = (ast.Expr) visit(ctx.multiplicativeExpr());
        return new ast.BinaryExpr("+", left, right);
    }

    @Override
    public ast.ASTNode visitMul(parser.MiniCppParser.MulContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.multiplicativeExpr());
        ast.Expr right = (ast.Expr) visit(ctx.unaryExpr());
        return new ast.BinaryExpr("*", left, right);
    }

    @Override
    public ast.ASTNode visitDiv(parser.MiniCppParser.DivContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.multiplicativeExpr());
        ast.Expr right = (ast.Expr) visit(ctx.unaryExpr());
        return new ast.BinaryExpr("/", left, right);
    }

    @Override
    public ast.ASTNode visitSub(parser.MiniCppParser.SubContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.additiveExpr());
        ast.Expr right = (ast.Expr) visit(ctx.multiplicativeExpr());
        return new ast.BinaryExpr("-", left, right);
    }

    @Override
    public ast.ASTNode visitMod(parser.MiniCppParser.ModContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.multiplicativeExpr());
        ast.Expr right = (ast.Expr) visit(ctx.unaryExpr());
        return new ast.BinaryExpr("%", left, right);
    }

    @Override
    public ast.ASTNode visitIfStmt(parser.MiniCppParser.IfStmtContext ctx) {
        ast.Expr cond = (ast.Expr) visit(ctx.expr());
        ast.Statement thenS = (ast.Statement) visit(ctx.stmt(0));
        ast.Statement elseS = (ctx.stmt().size() > 1) ? (ast.Statement) visit(ctx.stmt(1)) : null;
        return new ast.IfStmt(cond, thenS, elseS);
    }

    @Override
    public ast.ASTNode visitWhileStmt(parser.MiniCppParser.WhileStmtContext ctx) {
        ast.Expr cond = (ast.Expr) visit(ctx.expr());
        ast.Statement body = (ast.Statement) visit(ctx.stmt());
        return new ast.WhileStmt(cond, body);
    }

    @Override
    public ast.ASTNode visitExpr(parser.MiniCppParser.ExprContext ctx) {
        return visit(ctx.assignment());
    }

    @Override
    public ast.ASTNode visitAssignPass(parser.MiniCppParser.AssignPassContext ctx) {
        return visit(ctx.orExpr());
    }

    @Override
    public ast.ASTNode visitOrPass(parser.MiniCppParser.OrPassContext ctx) {
        return visit(ctx.andExpr());
    }

    @Override
    public ast.ASTNode visitAndPass(parser.MiniCppParser.AndPassContext ctx) {
        return visit(ctx.equalityExpr());
    }

    @Override
    public ast.ASTNode visitEqPass(parser.MiniCppParser.EqPassContext ctx) {
        return visit(ctx.relationalExpr());
    }

    @Override
    public ast.ASTNode visitRelPass(parser.MiniCppParser.RelPassContext ctx) {
        return visit(ctx.additiveExpr());
    }

    @Override
    public ast.ASTNode visitAddPass(parser.MiniCppParser.AddPassContext ctx) {
        return visit(ctx.multiplicativeExpr());
    }

    @Override
    public ast.ASTNode visitMulPass(parser.MiniCppParser.MulPassContext ctx) {
        return visit(ctx.unaryExpr());
    }

    @Override
    public ast.ASTNode visitUnaryPass(parser.MiniCppParser.UnaryPassContext ctx) {
        return visit(ctx.primary());
    }

    @Override
    public ast.ASTNode visitAssign(parser.MiniCppParser.AssignContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.orExpr());
        ast.Expr right = (ast.Expr) visit(ctx.assignment());
        return new ast.BinaryExpr("=", left, right);
    }

    //Equality == und !=
    @Override
    public ast.ASTNode visitEq(parser.MiniCppParser.EqContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.equalityExpr());
        ast.Expr right = (ast.Expr) visit(ctx.relationalExpr());
        return new ast.BinaryExpr("==", left, right);
    }

    @Override
    public ast.ASTNode visitNeq(parser.MiniCppParser.NeqContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.equalityExpr());
        ast.Expr right = (ast.Expr) visit(ctx.relationalExpr());
        return new ast.BinaryExpr("!=", left, right);
    }

    //Realational <, <=, >, >=
    @Override
    public ast.ASTNode visitLt(parser.MiniCppParser.LtContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.relationalExpr());
        ast.Expr right = (ast.Expr) visit(ctx.additiveExpr());
        return new ast.BinaryExpr("<", left, right);
    }

    @Override
    public ast.ASTNode visitLe(parser.MiniCppParser.LeContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.relationalExpr());
        ast.Expr right = (ast.Expr) visit(ctx.additiveExpr());
        return new ast.BinaryExpr("<=", left, right);
    }

    @Override
    public ast.ASTNode visitGt(parser.MiniCppParser.GtContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.relationalExpr());
        ast.Expr right = (ast.Expr) visit(ctx.additiveExpr());
        return new ast.BinaryExpr(">", left, right);
    }

    @Override
    public ast.ASTNode visitGe(parser.MiniCppParser.GeContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.relationalExpr());
        ast.Expr right = (ast.Expr) visit(ctx.additiveExpr());
        return new ast.BinaryExpr(">=", left, right);
    }

    // && / ||
    @Override
    public ast.ASTNode visitAnd(parser.MiniCppParser.AndContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.andExpr());
        ast.Expr right = (ast.Expr) visit(ctx.equalityExpr());
        return new ast.BinaryExpr("&&", left, right);
    }

    @Override
    public ast.ASTNode visitOr(parser.MiniCppParser.OrContext ctx) {
        ast.Expr left = (ast.Expr) visit(ctx.orExpr());
        ast.Expr right = (ast.Expr) visit(ctx.andExpr());
        return new ast.BinaryExpr("||", left, right);
    }

    @Override
    public ast.ASTNode visitReturnStmt(parser.MiniCppParser.ReturnStmtContext ctx) {
        ast.Expr e = (ctx.expr() != null) ? (ast.Expr) visit(ctx.expr()) : null;
        return new ast.ReturnStmt(e);
    }

    @Override
    public ast.ASTNode visitFuncCall(parser.MiniCppParser.FuncCallContext ctx) {
        String name = ctx.ID().getText();

        java.util.List<ast.Expr> args = new java.util.ArrayList<>();
        if (ctx.argList() != null) {
            for (parser.MiniCppParser.ExprContext e : ctx.argList().expr()) {
                args.add((ast.Expr) visit(e));
            }
        }

        return new ast.FunctionCallExpr(name, args);
    }

    @Override
    public ast.ASTNode visitIntType(parser.MiniCppParser.IntTypeContext ctx) {
        return new ast.IntTypeNode();
    }

    @Override
    public ast.ASTNode visitBoolType(parser.MiniCppParser.BoolTypeContext ctx) {
        return new ast.BoolTypeNode();
    }

    @Override
    public ast.ASTNode visitCharType(parser.MiniCppParser.CharTypeContext ctx) {
        return new ast.CharTypeNode();
    }

    @Override
    public ast.ASTNode visitStringType(parser.MiniCppParser.StringTypeContext ctx) {
        return new ast.StringTypeNode();
    }

    @Override
    public ast.ASTNode visitRefType(parser.MiniCppParser.RefTypeContext ctx) {
        ast.TypeNode base = (ast.TypeNode) visit(ctx.type());
        return new ast.RefTypeNode(base);
    }

    @Override
    public ast.ASTNode visitClassType(parser.MiniCppParser.ClassTypeContext ctx) {
        return new ast.ClassTypeNode(ctx.ID().getText());
    }

    @Override
    public ast.ASTNode visitBoolLiteral(parser.MiniCppParser.BoolLiteralContext ctx) {
        boolean v = ctx.BOOL().getText().equals("true");
        return new ast.BoolLiteral(v);
    }

    @Override
    public ast.ASTNode visitCharLiteral(parser.MiniCppParser.CharLiteralContext ctx) {
        String text = ctx.CHAR().getText();   // z.B. 'a' oder '\0'
        char c = parseCharLiteral(text);
        return new ast.CharLiteral(c);
    }

    @Override
    public ast.ASTNode visitStringLiteral(parser.MiniCppParser.StringLiteralContext ctx) {
        String raw = ctx.STRING().getText(); // inkl. ""
        String s = raw.substring(1, raw.length() - 1);
        s = unescapeString(s);
        return new ast.StringLiteral(s);
    }


    private char parseCharLiteral(String tokenText) {
        // tokenText inkl. Quotes, z.B.  'a'  oder  '\0'
        String inner = tokenText.substring(1, tokenText.length() - 1);

        if (inner.length() == 1 && inner.charAt(0) != '\\') {
            return inner.charAt(0);
        }

        // Escape-Sequenzen (minimal)
        if (inner.startsWith("\\")) {
            char esc = inner.length() >= 2 ? inner.charAt(1) : '\0';
            return switch (esc) {
                case '0' -> '\0';
                case 'n' -> '\n';
                case 't' -> '\t';
                case 'r' -> '\r';
                case '\\' -> '\\';
                case '\'' -> '\'';
                case '"' -> '"';
                default -> esc; // fallback
            };
        }

        return '\0';
    }

    private String unescapeString(String s) {
        // minimal: \" \\ \n \t \r \0
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < s.length()) {
                char esc = s.charAt(++i);
                out.append(switch (esc) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case 'r' -> '\r';
                    case '0' -> '\0';
                    case '\\' -> '\\';
                    case '"' -> '"';
                    default -> esc;
                });
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }


}
