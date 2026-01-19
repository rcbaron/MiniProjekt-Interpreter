package ast;

/**
 * Feld-Deklaration innerhalb einer Klasse.
 * Unterscheidet sich von VarDeclStmt dadurch, dass es kein Statement ist,
 * sondern spezifisch ein Attribut einer Klasse.
 *
 */
public class FieldDecl extends ASTNode {

    /** Der Datentyp des Feldes. */
    public final TypeNode type;

    /** Der Name des Feldes. */
    public final String name;

    public FieldDecl(TypeNode type, String name) {
        this.type = type;
        this.name = name;
    }
}
