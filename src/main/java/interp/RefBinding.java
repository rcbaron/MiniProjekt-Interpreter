package interp;

import ast.TypeNode;

public record RefBinding(TypeNode type, Cell target) implements Binding {
    @Override public Cell cell() { return target; } // Alias
}
