package ast;

/**
 * Die Verwendung einer Variablen (Lese- oder Schreibzugriff).
 * Beispiel: "x" in "x + 1" oder "x = 5".
 *
 */
public class VarExpr extends Expr {

    /** Der Name der referenzierten Variable. */
    public String name;

    public VarExpr(String name) {
        this.name = name;
    }
}
