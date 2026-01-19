package interp;

/**
 * Eine spezielle Exception, die fuer den Kontrollfluss "return" genutzt wird.
 * Wenn ein 'return stmt;' ausgefuehrt wird, wird diese Exception geworfen,
 * um sofort aus verschachtelten Bloecken/Schleifen herauszuspringen
 * und zur aufrufenden Funktion zurueckzukehren.
 *
 */
public class ReturnValue extends RuntimeException {

    // Der Rueckgabewert
    public final Object value;

    public ReturnValue(Object value) {
        this.value = value;
    }
}
