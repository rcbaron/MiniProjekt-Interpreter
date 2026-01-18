package ast;

public class Param {
    public final TypeNode type;
    public final String name;

    public Param(TypeNode type, String name) {
        this.type = type;
        this.name = name;
    }
}
