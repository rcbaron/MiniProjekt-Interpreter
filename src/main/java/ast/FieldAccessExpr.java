package ast;

public class FieldAccessExpr extends Expr {
    public final Expr obj;
    public final String field;

    public FieldAccessExpr(Expr obj, String field) {
        this.obj = obj;
        this.field = field;
    }
}
