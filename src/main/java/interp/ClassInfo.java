package interp;

import ast.TypeNode;
import java.util.*;

public final class ClassInfo {
    public final String name;
    public final String baseName; // null wenn keine
    public final LinkedHashMap<String, TypeNode> fields = new LinkedHashMap<>();
    // name -> overloads
    public final Map<String, List<MethodInfo>> methods = new HashMap<>();
    public final List<CtorInfo> ctors = new ArrayList<>();

    public ClassInfo(String name, String baseName) {
        this.name = name;
        this.baseName = baseName;
    }
}
