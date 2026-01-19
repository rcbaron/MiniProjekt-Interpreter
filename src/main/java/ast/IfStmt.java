package ast;

/**
 * If-Anweisung.
 * Syntax: if (condition) thenStmt [else elseStmt]
 *
 */
public class IfStmt extends Statement {

    /** Die Bedingung (muss zu bool auswertbar sein). */
    public final Expr cond;

    /** Der Rumpf, der ausgefuehrt wird, wenn die Bedingung wahr ist. */
    public final Statement thenStmt;

    /** * Der Rumpf fuer den Else-Fall.
     * Ist null, wenn kein 'else' vorhanden ist.
     */
    public final Statement elseStmt;

    public IfStmt(Expr cond, Statement thenStmt, Statement elseStmt) {
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }
}

