package ast;

public class UnaryExpr extends Expr {
    public String op; // z.B. "-", "!"
    public Expr expr;

    public UnaryExpr(String op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }
}
