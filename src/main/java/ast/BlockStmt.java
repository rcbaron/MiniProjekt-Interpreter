package ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Block von Anweisungen innerhalb von geschweiften Klammern {...}.
 * Ein Block eröffnet in C++ einen neuen Scope (Gueltigkeitsbereich)
 * fuer lokale Variablen.
 *
 */
public class BlockStmt extends Statement {

    /** Die Liste der Statements innerhalb des Blocks. */
    public final List<Statement> statements = new ArrayList<>();
}
