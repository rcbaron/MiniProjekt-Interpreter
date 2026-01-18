package ast;

public class VarDeclStmt extends Statement {
    public String name;
    public TypeNode type;
    public Expr init; // kann null sein

    public VarDeclStmt(String name, TypeNode type, Expr init) {
        this.name = name;
        this.type = type;
        this.init = init;
    }
}
