package interp;

import ast.TypeNode;

public record ValueBinding(TypeNode type, Cell cell) implements Binding {}
