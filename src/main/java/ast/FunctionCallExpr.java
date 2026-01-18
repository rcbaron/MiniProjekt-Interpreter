package ast;

import java.util.List;

/**
 * Globaler Funktionsaufruf.
 * Syntax: funcName(arg1, arg2, ...)
 *
 */
public class FunctionCallExpr extends Expr {

    /** Der Name der aufgerufenen Funktion. */
    public final String name;

    /** Die Liste der uebergebenen Argumente (Ausdruecke). */
    public final List<Expr> args;

    public FunctionCallExpr(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }
}
