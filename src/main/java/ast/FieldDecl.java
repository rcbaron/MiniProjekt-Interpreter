package ast;

public class FieldDecl extends ASTNode {
    public final TypeNode type;
    public final String name;

    public FieldDecl(TypeNode type, String name) {
        this.type = type;
        this.name = name;
    }
}
