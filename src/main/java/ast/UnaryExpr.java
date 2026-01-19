package ast;

/**
 * Unaerer Ausdruck (Operator mit nur einem Operanden).
 * Beispiele: "-5" (Negation) oder "!true" (logisches Nicht).
 *
 */
public class UnaryExpr extends Expr {

    /** Der Operator als String (z.B. "-", "!"). */
    public String op;

    /** Der Ausdruck, auf den der Operator angewendet wird. */
    public Expr expr;

    public UnaryExpr(String op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }
}
