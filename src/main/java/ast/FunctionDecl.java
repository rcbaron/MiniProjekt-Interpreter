package ast;

import java.util.List;

/**
 * Repraesentiert eine Funktions- oder Methodendeklaration.
 * Beispiel: "int add(int a, int b) { return a+b; }"
 * oder "virtual void speak() { ... }" innerhalb einer Klasse.
 *
 */
public class FunctionDecl extends ASTNode {

    /** Der Name der Funktion/Methode. */
    public final String name;

    /** Die Liste der Parameter (Typ und Name). */
    public final List<Param> params;

    /** Der Funktionskoerper (Block mit Statements). */
    public final BlockStmt body;

    /**
     * Gibt an, ob die Methode als 'virtual' deklariert wurde.
     * Wichtig fuer dynamischen Dispatch (Polymorphie) in C++.
     */
    public final boolean isVirtual;


    public FunctionDecl(String name, List<Param> params, BlockStmt body, boolean isVirtual) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.isVirtual = isVirtual;
    }

}
