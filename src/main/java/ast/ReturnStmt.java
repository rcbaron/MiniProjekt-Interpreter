package ast;

public class ReturnStmt extends Statement {
    public final Expr expr; // kann null sein (return;)

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }
}

