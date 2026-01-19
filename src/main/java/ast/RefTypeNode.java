package ast;

/**
 * Referenz-Typ.
 * Syntax: T& (z.B. int&, MyClass&).
 * Wichtig fuer Call-by-Reference Parameter und Referenz-Variablen.
 *
 */
public class RefTypeNode extends TypeNode {

    /** Der Basistyp, auf den referenziert wird (z.B. IntTypeNode). */
    public final TypeNode base;

    public RefTypeNode(TypeNode base) {
        this.base = base;
    }
}
