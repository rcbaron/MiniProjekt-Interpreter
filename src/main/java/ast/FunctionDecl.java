package ast;

public class FunctionDecl extends ASTNode {
    public final String name;
    public final BlockStmt body;

    public FunctionDecl(String name, BlockStmt body) {
        this.name = name;
        this.body = body;
    }
}
