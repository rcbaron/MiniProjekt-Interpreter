package ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Der Wurzelknoten des AST.
 * Repraesentiert das gesamte geparste Programm (oder eine REPL-Eingabe).
 * Enthaelt eine Liste von Deklarationen (Funktionen, Klassen) oder
 * Statements (im Fall der REPL/Main-Logik).
 *
 */
public class Program extends ASTNode {

    /**
     * Die Liste aller Top-Level-Elemente in der Datei/Eingabe.
     * Kann ClassDecl, FunctionDecl oder Statement Objekte enthalten.
     */
    public final List<ASTNode> declarations = new ArrayList<>();
}
