package ast;
import java.util.List;

/**
 * Expliziter Aufruf eines Konstruktors als Ausdruck.
 * Syntax-Beispiel: "T(args)" in "T x = T(args);"
 * Dient der Erzeugung neuer Objekt-Instanzen.
 *
 */
public class CtorCallExpr extends Expr {

    /** Der Name der Klasse, die instanziiert wird. */
    public final String className;

    /** Die Argumente fuer den Konstruktor. */
    public final List<Expr> args;

    public CtorCallExpr(String className, List<Expr> args) {
        this.className = className;
        this.args = args;
    }
}
