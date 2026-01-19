package ast;

/**
 * String-Literal (Zeichenkette).
 * Beispiel: "Hallo Welt"
 *
 */
public class StringLiteral extends Expr {
    public final String value;

    public StringLiteral(String value) {
        this.value = value;
    }
}
