package ast;

/**
 * Zugriff auf ein oeffentliches Feld eines Objekts.
 * Syntax: object.fieldName
 *
 */
public class FieldAccessExpr extends Expr {

    /** Das Objekt, dessen Feld gelesen/geschrieben wird. */
    public final Expr obj;

    /** Der Name des Feldes. */
    public final String field;

    public FieldAccessExpr(Expr obj, String field) {
        this.obj = obj;
        this.field = field;
    }
}
