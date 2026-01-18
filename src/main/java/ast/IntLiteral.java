package ast;

/**
 * Ganzzahl-Literal.
 * Beispiel: 42, 0, -1
 *
 */
public class IntLiteral extends Expr {
    public final int value;

    public IntLiteral(int value) {
        this.value = value;
    }
}
