package ast;

import java.util.List;

/**
 * Methodenaufruf auf einem Objekt.
 * Syntax: object.methodName(args)
 * Fuer OOP und Polymorphie.
 *
 */
public class MethodCallExpr extends Expr {

    /** Das Objekt, auf dem die Methode aufgerufen wird (Receiver). */
    public final Expr obj;

    /** Der Name der Methode. */
    public final String method;

    /** Die Argumente des Aufrufs. */
    public final List<Expr> args;

    public MethodCallExpr(Expr obj, String method, List<Expr> args) {
        this.obj = obj;
        this.method = method;
        this.args = args;
    }
}
