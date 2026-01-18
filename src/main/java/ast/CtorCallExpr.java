package ast;
import java.util.List;

public class CtorCallExpr extends Expr {
    public final String className;
    public final List<Expr> args;

    public CtorCallExpr(String className, List<Expr> args) {
        this.className = className;
        this.args = args;
    }
}
