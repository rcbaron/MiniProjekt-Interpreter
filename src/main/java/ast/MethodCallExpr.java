package ast;

import java.util.List;

public class MethodCallExpr extends Expr {
    public final Expr obj;
    public final String method;
    public final List<Expr> args;

    public MethodCallExpr(Expr obj, String method, List<Expr> args) {
        this.obj = obj;
        this.method = method;
        this.args = args;
    }
}
