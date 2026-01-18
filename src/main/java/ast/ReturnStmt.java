package ast;

/**
 * Return-Anweisung.
 * Beendet die aktuelle Funktionsausfuehrung und gibt ggf. einen Wert zurueck.
 *
 */
public class ReturnStmt extends Statement {

    /** * Der Rueckgabewert.
     * Ist null bei 'return;' (void-Funktionen).
     */
    public final Expr expr;

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }
}

