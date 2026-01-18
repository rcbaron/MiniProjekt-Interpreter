package ast;

/**
 * Variablendeklaration.
 * Syntax: Typ Name [= InitExpression];
 * Beispiel: "int x = 5;" oder "MyClass c;"
 *
 */
public class VarDeclStmt extends Statement {

    /** Der Name der Variable. */
    public String name;

    /** Der Datentyp der Variable (z.B. IntTypeNode, ClassTypeNode). */
    public TypeNode type;

    /**
     * Der Initialisierungsausdruck.
     * Kann null sein, wenn keine explizite Zuweisung erfolgt (z.B. "int x;").
     */
    public Expr init;

    public VarDeclStmt(String name, TypeNode type, Expr init) {
        this.name = name;
        this.type = type;
        this.init = init;
    }
}
