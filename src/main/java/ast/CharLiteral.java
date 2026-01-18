package ast;

public class CharLiteral extends Expr {
    public final char value;

    public CharLiteral(char value) {
        this.value = value;
    }
}
