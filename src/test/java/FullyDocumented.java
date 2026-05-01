/**
 * Eine Klasse zur Demonstration von vollständiger Javadoc-Dokumentation.
 * Alle Methoden sind hier umfassend beschrieben.
 */
public class FullyDocumented {

    /**
     * Berechnet die Summe zweier ganzer Zahlen.
     *
     * @param a Die erste ganze Zahl.
     * @param b Die zweite ganze Zahl.
     * @return Die Summe von a und b.
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * Berechnet das Produkt zweier ganzer Zahlen.
     *
     * @param x Der erste Faktor.
     * @param y Der zweite Faktor.
     * @return Das Produkt von x und y.
     */
    public int multiply(int x, int y) {
        return x * y;
    }

    /**
     * Prüft, ob eine Zahl gerade ist.
     *
     * @param number Die zu prüfende Zahl.
     * @return true, wenn die Zahl gerade ist, sonst false.
     */
    public boolean isEven(int number) {
        return number % 2 == 0;
    }
}