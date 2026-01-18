package interp;

import ast.TypeNode;

public sealed interface Binding permits ValueBinding, RefBinding {
    TypeNode type();
    Cell cell();
}
