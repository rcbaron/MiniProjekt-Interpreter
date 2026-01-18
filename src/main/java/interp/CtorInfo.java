package interp;

import ast.*;
import java.util.*;

public final class CtorInfo {
    public final String className;
    public final List<Param> params;
    public final BlockStmt body;

    public CtorInfo(String className, List<Param> params, BlockStmt body) {
        this.className = className; this.params = params; this.body = body;
    }
}