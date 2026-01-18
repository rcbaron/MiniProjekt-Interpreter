package ast;

import java.util.List;

public class FunctionDecl extends ASTNode {
    public final String name;
    public final List<Param> params;
    public final BlockStmt body;
    public final boolean isVirtual;


    public FunctionDecl(String name, List<Param> params, BlockStmt body, boolean isVirtual) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.isVirtual = isVirtual;
    }

}
