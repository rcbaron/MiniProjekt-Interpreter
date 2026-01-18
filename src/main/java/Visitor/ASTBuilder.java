package Visitor;

import ast.*;
import parser.MiniCppBaseVisitor;
import parser.MiniCppParser;

public class ASTBuilder extends MiniCppBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitProgram(MiniCppParser.ProgramContext ctx) {
        Program p = new Program();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            var ch = ctx.getChild(i);

            if (ch instanceof MiniCppParser.FunctionDeclContext f) {
                p.declarations.add((ASTNode) visit(f));
            } else if (ch instanceof MiniCppParser.ClassDeclContext c) {
                p.declarations.add((ASTNode) visit(c));
            } else if (ch instanceof MiniCppParser.StmtContext s) {
                p.declarations.add((ASTNode) visit(s));
            }
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
        boolean isVirtual = (ctx.getStart().getText().equals("virtual"));
        return new ast.FunctionDecl(name, params, body, isVirtual);
    }

    @Override
    public ASTNode visitClassDecl(MiniCppParser.ClassDeclContext ctx) {
        // class ID (':' public ID)? '{' classMember* '}'
        String name = ctx.ID(0).getText();

        String baseName = null;
        if (ctx.ID().size() > 1) {
            baseName = ctx.ID(1).getText();
        }

        java.util.List<ASTNode> members = new java.util.ArrayList<>();
        for (MiniCppParser.ClassMemberContext m : ctx.classMember()) {
            members.add((ASTNode) visit(m));
        }

        return new ast.ClassDecl(name, baseName, members);
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
    public ASTNode visitUnaryPass(MiniCppParser.UnaryPassContext ctx) {
        return visit(ctx.postfix());
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
    public ASTNode visitCallOrCtor(MiniCppParser.CallOrCtorContext ctx) {
        String name = ctx.ID().getText();

        java.util.List<ast.Expr> args = new java.util.ArrayList<>();
        if (ctx.argList() != null) {
            for (MiniCppParser.ExprContext e : ctx.argList().expr()) {
                args.add((ast.Expr) visit(e));
            }
        }

        // Minimal (für jetzt): alles als FunctionCallExpr bauen.
        // Konstruktor-Call behandeln wir später im Interpreter / oder per Klassenname-Check.
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

    @Override
    public ASTNode visitPostfix(MiniCppParser.PostfixContext ctx) {
        // start: atom
        ast.Expr cur = (ast.Expr) visit(ctx.atom());

        // Danach kommt eine Folge von: '.' ID ('(' argList? ')')?
        // In ANTLR bekommst du die IDs als Liste:
        // - ctx.ID(i)
        // Und die optionalen argLists als Liste von children – easiest:
        // Wir laufen über die Kinder sequenziell.

        int i = 1; // child 0 ist atom
        while (i < ctx.getChildCount()) {
            String dot = ctx.getChild(i).getText(); // "."
            if (!".".equals(dot)) break;

            String name = ctx.getChild(i + 1).getText(); // ID nach dem Punkt

            // Methode oder Feld?
            // Wenn danach "(" kommt -> MethodCall, sonst FieldAccess
            boolean isCall = (i + 2 < ctx.getChildCount()) && "(".equals(ctx.getChild(i + 2).getText());

            if (isCall) {
                // args können leer sein: "()"
                java.util.List<ast.Expr> args = new java.util.ArrayList<>();

                // Wenn nicht direkt ")" kommt, dann steckt da argList
                if (i + 3 < ctx.getChildCount() && !")".equals(ctx.getChild(i + 3).getText())) {
                    // argList-context ist irgendwo als Kind; einfacher: visit über ctx.argList(k)
                    // Wir ziehen den nächsten ArgListContext in Reihenfolge:
                    // -> dafür zählen wir, wie viele MethodCalls wir schon verarbeitet haben.
                    // Einfacher Ansatz: parse args über MiniCppParser.ArgListContext aus dem subtree:
                    MiniCppParser.ArgListContext al = null;
                    for (int j = i; j < ctx.getChildCount(); j++) {
                        if (ctx.getChild(j) instanceof MiniCppParser.ArgListContext) {
                            al = (MiniCppParser.ArgListContext) ctx.getChild(j);
                            break;
                        }
                        if (")".equals(ctx.getChild(j).getText())) break;
                    }
                    if (al != null) {
                        for (MiniCppParser.ExprContext ectx : al.expr()) {
                            args.add((ast.Expr) visit(ectx));
                        }
                    }
                }

                cur = new ast.MethodCallExpr(cur, name, args);

                // skip: . ID ( ... )
                // wir springen bis nach der schließenden ")"
                int j = i + 2;
                while (j < ctx.getChildCount() && !")".equals(ctx.getChild(j).getText())) j++;
                i = j + 1;
            } else {
                cur = new ast.FieldAccessExpr(cur, name);
                i = i + 2; // skip: . ID
            }
        }

        return cur;
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
