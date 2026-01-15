package ast;

public class BoolLiteral extends Expr {
    public boolean value;

    public BoolLiteral(boolean value) {
        this.value = value;
    }
}
