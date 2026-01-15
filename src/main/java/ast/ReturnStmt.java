package ast;

public class ReturnStmt extends Statement {
    public Expr expr; // null wenn void return

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }
}
