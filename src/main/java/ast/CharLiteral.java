package ast;

/**
 * Character-Literal (einzelnes Zeichen).
 * Beispiel: 'a', '\n', '0'
 *
 */
public class CharLiteral extends Expr {
    public final char value;

    public CharLiteral(char value) {
        this.value = value;
    }
}
