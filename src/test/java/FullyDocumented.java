/**
 * Eine Klasse zur Demonstration von vollständiger Javadoc-Dokumentation.
 * Alle Methoden sind hier umfassend beschrieben.
 */
public class FullyDocumented {

    /**
     * Berechnet die Summe der beiden übergebenen ganzzahligen Parameter.
     * Diese Methode führt eine einfache Addition durch und gibt das exakte Ergebnis als Integer zurück.
     * Beachten Sie, dass bei der Addition von sehr großen Werten ein Integer-Overflow
     * (Wertebereichsüberschreitung) auftreten kann, da dieser Fall hier nicht explizit abgefangen wird.
     *
     * @param a Die erste ganze Zahl (Summand).
     * @param b Die zweite ganze Zahl (Summand).
     * @return Die berechnete Summe von a und b.
     * @throws RuntimeException test
     */
    public int add(int a, int b) throws RuntimeException {
        return a + b;
    }

    /**
     * Berechnet das mathematische Produkt aus zwei übergebenen ganzzahligen Faktoren.
     * Die Methode multipliziert den ersten Parameter mit dem zweiten und liefert das
     * resultierende Ergebnis zurück. Auch hier gelten die üblichen Grenzen des
     * Java-Datentyps 'int' bezüglich eines möglichen Überlaufs (Overflow) bei der
     * Multiplikation von sehr großen Zahlen.
     *
     * @param x Der erste Faktor der Multiplikation.
     * @param y Der zweite Faktor der Multiplikation.
     * @return Das berechnete Produkt von x und y.
     */
    public int multiply(int x, int y) {
        return x * y;
    }

    /**
     * Überprüft, ob eine gegebene ganze Zahl gerade ist.
     * Dies geschieht durch die Auswertung einer Modulo-2-Operation (number % 2 == 0).
     * Wenn der Rest der Division durch 2 exakt 0 ergibt, gilt die Zahl als gerade.
     * Die Methode funktioniert zuverlässig für positive Zahlen, negative Zahlen
     * sowie für die Null (welche mathematisch als gerade betrachtet wird).
     *
     * @param number Die zu prüfende ganze Zahl.
     * @return true, wenn die Zahl ohne Rest durch 2 teilbar ist (gerade), ansonsten false (ungerade).
     */
    public boolean isEven(int number) {
        return number % 2 == 0;
    }
}