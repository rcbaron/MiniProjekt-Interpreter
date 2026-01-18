package ast;

public class RefTypeNode extends TypeNode {
    public final TypeNode base;
    public RefTypeNode(TypeNode base) {
        this.base = base;
    }
}
