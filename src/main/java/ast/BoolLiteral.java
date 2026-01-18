package ast;

/**
 * Boolean-Literal.
 * Werte: true, false
 *
 */
public class BoolLiteral extends Expr {
    public final boolean value;

    public BoolLiteral(boolean value) {
        this.value = value;
    }
}
