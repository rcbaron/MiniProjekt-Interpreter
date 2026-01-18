package ast;

/**
 * Benutzerdefinierter Klassentyp.
 * Wird verwendet, wenn eine Variable vom Typ einer Klasse deklariert wird.
 * Beispiel: "MyClass x;" -> name = "MyClass"
 *
 */
public class ClassTypeNode extends TypeNode {

    /** Der Name der Klasse. */
    public final String name;

    public ClassTypeNode(String name) {
        this.name = name;
    }
}
