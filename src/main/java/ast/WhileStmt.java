package ast;

/**
 * While-Schleife.
 * Syntax: while (condition) body
 *
 */
public class WhileStmt extends Statement {

    /** Die Schleifenbedingung. Wird vor jedem Durchlauf geprueft. */
    public final Expr cond;

    /** Der Schleifenrumpf (meist ein BlockStmt). */
    public final Statement body;

    public WhileStmt(Expr cond, Statement body) {
        this.cond = cond;
        this.body = body;
    }
}
