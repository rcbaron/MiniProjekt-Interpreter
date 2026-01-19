package ast;
import java.util.List;

/**
 * Methoden-Deklaration.
 * Beinhaltet explizit einen Rueckgabetypen (returnType).
 *
 */
public class MethodDecl extends ASTNode {

    /** Ob die Methode virtual ist (für Polymorphie). */
    public final boolean isVirtual;

    /** Der Rueckgabetyp der Methode (z.B. IntTypeNode, VoidTypeNode). */
    public final TypeNode returnType;

    /** Der Methodenname. */
    public final String name;

    /** Die Parameterliste. */
    public final List<Param> params;

    /** Der Body der Methode. */
    public final BlockStmt body;

    public MethodDecl(boolean isVirtual, TypeNode returnType, String name, List<Param> params, BlockStmt body) {
        this.isVirtual = isVirtual;
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.body = body;
    }
}
