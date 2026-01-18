package interp;

import ast.TypeNode;

/**
 * Eine normale Variablen-Bindung (Value Binding).
 * Beispiel: "int x = 5;"
 * Die Variable 'x' besitzt ihre eigene Speicherzelle.
 *
 */
public record ValueBinding(TypeNode type, Cell cell) implements Binding {}
