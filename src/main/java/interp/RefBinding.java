package interp;

import ast.TypeNode;

/**
 * Eine Referenz-Bindung (Reference Binding).
 * Beispiel: "int& ref = x;"
 * Die Variable 'ref' hat keine eigene Zelle, sondern zeigt auf die Zelle von 'x'.
 *
 */
public record RefBinding(TypeNode type, Cell target) implements Binding {
    @Override public Cell cell() { return target; }
}
