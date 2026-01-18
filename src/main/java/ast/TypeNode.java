package ast;

/**
 * Abstrakte Basisklasse fuer Typ-Informationen im AST.
 * Wird verwendet, um Typen bei Variablendeklarationen, Parametern
 * oder Rueckgabetypen zu beschreiben (z.B. int, bool, MyClass, int&).
 *
 */
public abstract class TypeNode extends ASTNode {}
