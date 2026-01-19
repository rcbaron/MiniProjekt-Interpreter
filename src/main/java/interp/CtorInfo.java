package interp;

import ast.*;
import java.util.*;

/**
 * Metadaten fuer einen Konstruktor.
 * Wird verwendet, um bei 'new A(1, 2)' den passenden Konstruktor zu finden.
 *
 */
public final class CtorInfo {
    public final String className;
    public final List<Param> params;
    public final BlockStmt body;

    public CtorInfo(String className, List<Param> params, BlockStmt body) {
        this.className = className;
        this.params = params;
        this.body = body;
    }
}