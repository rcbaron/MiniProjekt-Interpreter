package ast;

public class StringLiteral extends Expr {
    public final String value;

    public StringLiteral(String value) {
        this.value = value;
    }
}
