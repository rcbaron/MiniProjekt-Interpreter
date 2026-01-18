package ast;

import java.util.ArrayList;
import java.util.List;

public class ClassDecl extends ASTNode {
    public final String name;
    public final String baseName; // null wenn keine
    public final List<ASTNode> members = new ArrayList<>(); // VarDeclStmt oder FunctionDecl

    public ClassDecl(String name, String baseName, List<ASTNode> members) {
        this.name = name;
        this.baseName = baseName;
        this.members.addAll(members);
    }
}
