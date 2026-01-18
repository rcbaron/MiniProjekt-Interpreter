package ast;

/**
 * Binaerer Ausdruck (zwei Operanden und ein Operator).
 * Deckt Arithmetik (+, -, *, ...), Vergleiche (==, <, ...) und Logik (&&, ||) ab,
 * sowie Zuweisungen (=).
 *
 */
public class BinaryExpr extends Expr {

    /** Der Operator als String (z.B. "+", "==", "&&", "="). */
    public final String op;

    /** Der linke Operand. */
    public final Expr left;

    /** Der rechte Operand. */
    public final Expr right;

    public BinaryExpr(String op, Expr left, Expr right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }
}
