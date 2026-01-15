package ast;

import java.util.List;

public class FunctionDecl extends ASTNode {
    public final String name;
    public final List<Param> params;
    public final BlockStmt body;

    public FunctionDecl(String name, List<Param> params, BlockStmt body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }
}
