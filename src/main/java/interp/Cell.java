package interp;

public final class Cell {
    private Object value;

    public Cell(Object value) {
        this.value = value;
    }

    public Object get() { return value; }
    public void set(Object value) { this.value = value; }
}
