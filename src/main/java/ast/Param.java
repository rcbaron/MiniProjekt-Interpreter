package ast;

/**
 * Eine Hilfsklasse fuer Parameter-Listen in Funktionen/Konstruktoren.
 * Speichert die Kombination aus Typ und Name (z.B. "int x").
 *
 */
public class Param {

    /** Der Typ des Parameters (kann auch ein RefTypeNode sein). */
    public final TypeNode type;

    /** Der Name des Parameters. */
    public final String name;

    public Param(TypeNode type, String name) {
        this.type = type;
        this.name = name;
    }
}
