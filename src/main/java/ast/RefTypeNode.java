package ast;

public class RefTypeNode extends TypeNode {
    public TypeNode baseType;
    public RefTypeNode(TypeNode baseType) { this.baseType = baseType; }
}