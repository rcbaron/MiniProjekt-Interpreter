package ast;

import java.util.List;

public class FunctionCallExpr extends Expr {
    public final String name;
    public final List<Expr> args;

    public FunctionCallExpr(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }
}
