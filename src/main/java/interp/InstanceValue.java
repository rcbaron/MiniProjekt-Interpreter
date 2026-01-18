package interp;

import java.util.*;

public final class InstanceValue {
    public final String dynamicClass; // tatsächlicher Typ (z.B. D)
    public final LinkedHashMap<String, Cell> fieldCells;

    public InstanceValue(String dynamicClass, LinkedHashMap<String, Cell> fieldCells) {
        this.dynamicClass = dynamicClass;
        this.fieldCells = fieldCells;
    }

    public InstanceValue deepCopy() {
        var copy = new LinkedHashMap<String, Cell>();
        for (var e : fieldCells.entrySet()) copy.put(e.getKey(), new Cell(e.getValue().get()));
        return new InstanceValue(dynamicClass, copy);
    }
}
