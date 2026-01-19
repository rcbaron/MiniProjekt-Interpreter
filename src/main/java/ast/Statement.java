package ast;

/**
 * Abstrakte Basisklasse fuer alle Anweisungen (Statements).
 * Statements fuehren Aktionen aus (z.B. Zuweisungen, Schleifen, if-Bloecke),
 * haben aber selbst keinen direkten Rueckgabewert im Sinne eines Ausdrucks.
 *
 */
public abstract class Statement extends ASTNode {}
