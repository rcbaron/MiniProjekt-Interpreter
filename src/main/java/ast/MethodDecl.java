package ast;
import java.util.List;

public class MethodDecl extends ASTNode {
    public final boolean isVirtual;
    public final TypeNode returnType;
    public final String name;
    public final List<Param> params;
    public final BlockStmt body;

    public MethodDecl(boolean isVirtual, TypeNode returnType, String name, List<Param> params, BlockStmt body) {
        this.isVirtual = isVirtual;
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.body = body;
    }
}
