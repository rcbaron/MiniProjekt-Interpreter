package ast;

import java.util.List;

/**
 * Konstruktor-Deklaration innerhalb einer Klasse.
 * Syntax: ClassName(params) {body}
 * Unterscheidet sich von FunctionDecl durch fehlenden Rueckgabetyp und festen Namen.
 *
 */
public class ConstructorDecl extends ASTNode {

    /** Der Name der Klasse (muss identisch mit dem Klassennamen sein). */
    public final String className;

    /** Liste der Parameter. */
    public final List<Param> params;

    /** Der Code, der zur Initialisierung ausgefuehrt wird. */
    public final BlockStmt body;

    public ConstructorDecl(String className, List<Param> params, BlockStmt body) {
        this.className = className;
        this.params = params;
        this.body = body;
    }
}
