package ast;

public class VarExpr extends Expr {
    public String name;

    public VarExpr(String name) {
        this.name = name;
    }
}
