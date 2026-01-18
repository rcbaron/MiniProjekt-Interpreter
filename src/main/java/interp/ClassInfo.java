package interp;

import ast.TypeNode;
import java.util.*;

/**
 * Speichert alle Laufzeit-Informationen ueber eine definierte Klasse.
 * Dient als Blueprint fuer die Erstellung von InstanceValues.
 *
 */
public final class ClassInfo {
    public final String name;

    // Name der Elternklasse (oder null)
    public final String baseName;

    // Felder der Klasse (Name -> Typ)
    public final LinkedHashMap<String, TypeNode> fields = new LinkedHashMap<>();

    // Methoden der Klasse (Name -> Liste von Ueberladungen)
    public final Map<String, List<MethodInfo>> methods = new HashMap<>();

    // Konstruktoren der Klasse
    public final List<CtorInfo> ctors = new ArrayList<>();

    public ClassInfo(String name, String baseName) {
        this.name = name;
        this.baseName = baseName;
    }
}
