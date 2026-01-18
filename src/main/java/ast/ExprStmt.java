package ast;

/**
 * Ein "Expression Statement".
 * Dies ermoeglicht es, einen Ausdruck als eigenstaendige Anweisung zu nutzen.
 * Wichtig fuer Zuweisungen ("x = 5;") oder Funktionsaufrufe ("print(x);"),
 * die syntaktisch Ausdruecke sind, aber meist als Statement stehen.
 *
 */
public class ExprStmt extends Statement {

    /** Der Ausdruck, der ausgefuehrt (und dessen Ergebnis meist verworfen) wird. */
    public final Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }
}
