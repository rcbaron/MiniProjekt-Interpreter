package ast;

public class BinaryExpr extends Expr {
    public final String op;
    public final Expr left;
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
