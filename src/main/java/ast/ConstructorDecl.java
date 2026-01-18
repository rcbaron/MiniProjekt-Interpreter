package ast;
import java.util.List;

public class ConstructorDecl extends ASTNode {
    public final String className;
    public final List<Param> params;
    public final BlockStmt body;

    public ConstructorDecl(String className, List<Param> params, BlockStmt body) {
        this.className = className;
        this.params = params;
        this.body = body;
    }
}
