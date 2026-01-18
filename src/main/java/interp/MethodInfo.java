package interp;

import ast.*;
import java.util.*;

public final class MethodInfo {
    public final String name;
    public final TypeNode returnType;
    public final List<Param> params;
    public final BlockStmt body;
    public final boolean isVirtual;
    public final String definedIn; // Klassenname

    public MethodInfo(String name, TypeNode returnType, List<Param> params, BlockStmt body, boolean isVirtual, String definedIn) {
        this.name = name; this.returnType = returnType; this.params = params; this.body = body;
        this.isVirtual = isVirtual; this.definedIn = definedIn;
    }
}
