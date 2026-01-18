package interp;

import java.util.*;

/**
 * Objekt (Instanz einer Klasse) zur Laufzeit.
 * Speichert den Namen der Klasse (fuer dynamischen Dispatch/Type-Checks)
 * und die Werte der Felder.
 *
 */
public final class InstanceValue {

    // Der dynamische Typ des Objekts (Name der Klasse, von der es instanziiert wurde)
    public final String dynamicClass;

    // Die Felder des Objekts. Map: Feldname -> Speicherzelle.
    // Nutzung von Cell ermoeglicht, dass Felder mutable sind.
    public final LinkedHashMap<String, Cell> fieldCells;

    public InstanceValue(String dynamicClass, LinkedHashMap<String, Cell> fieldCells) {
        this.dynamicClass = dynamicClass;
        this.fieldCells = fieldCells;
    }

    // Erstellt eine tiefe Kopie ("Pass-by-Value" von Objekten in C++)
    public InstanceValue deepCopy() {
        var copy = new LinkedHashMap<String, Cell>();
        for (var e : fieldCells.entrySet()) copy.put(e.getKey(), new Cell(e.getValue().get()));
        return new InstanceValue(dynamicClass, copy);
    }
}
