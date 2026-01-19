package ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Repraesentiert eine Klassendefinition (class A: public B{...}).
 * Speichert den Klassennamen, die Basisklasse (fuer Vererbung) und alle Member.
 *
 */
public class ClassDecl extends ASTNode {

    /** Der Name der Klasse (z.B. "MyClass"). */
    public final String name;

    /**
     * Der Name der Basisklasse, von der geerbt wird.
     * Ist null, wenn keine Vererbung stattfindet.
     */
    public final String baseName;

    /**
     * Liste der Klassen-Member.
     * Enthaelt typischerweise VarDeclStmt (Felder) und FunctionDecl (Methoden).
     */
    public final List<ASTNode> members = new ArrayList<>();

    public ClassDecl(String name, String baseName, List<ASTNode> members) {
        this.name = name;
        this.baseName = baseName;
        this.members.addAll(members);
    }
}
