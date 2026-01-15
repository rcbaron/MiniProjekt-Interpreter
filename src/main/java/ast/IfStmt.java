package ast;

public class IfStmt extends Statement {
    public final Expr cond;
    public final Statement thenStmt;
    public final Statement elseStmt; // kann null sein

    public IfStmt(Expr cond, Statement thenStmt, Statement elseStmt) {
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }
}

