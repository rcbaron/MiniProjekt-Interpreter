package interp;

import ast.TypeNode;

/**
 * Interface fuer eine Variablen-Bindung im Scope.
 * Verknuepft einen Namen mit einem Typ und einer Speicherzelle.
 *
 */
public sealed interface Binding permits ValueBinding, RefBinding {

    // Der statische Typ der Variable
    TypeNode type();

    // Zugriff auf den Speicherort
    Cell cell();
}
