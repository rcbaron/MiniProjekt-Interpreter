package ast;

public class ExprStmt extends Statement {
    public final Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }
}
