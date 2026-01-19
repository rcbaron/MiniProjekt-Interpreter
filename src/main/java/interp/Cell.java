package interp;

/**
 * Repraesentiert eine "Speicherzelle".
 * Ein Cell-Objekt haelt den tatsaechlichen Wert zur Laufzeit.
 * Mehrere Variablen (Bindings) koennen auf dieselbe Cell zeigen (Aliasing durch Referenzen).
 *
 */
public final class Cell {

    // Der Laufzeitwert (Integer, Boolean, InstanceValue, etc.)
    private Object value;

    public Cell(Object value) {
        this.value = value;
    }

    public Object get() { return value; }
    public void set(Object value) { this.value = value; }
}
