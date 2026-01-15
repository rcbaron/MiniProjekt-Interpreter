package ast;

public class WhileStmt extends Statement {
    public final Expr cond;
    public final Statement body;

    public WhileStmt(Expr cond, Statement body) {
        this.cond = cond;
        this.body = body;
    }
}
