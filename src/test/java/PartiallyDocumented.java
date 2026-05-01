public class PartiallyDocumented {

    /**
     * Konvertiert einen String in Großbuchstaben.
     *
     * @param text Der Eingabetext.
     * @return Der Text in Großbuchstaben.
     */
    public String toUpperCase(String text) {
        if (text == null) return null;
        return text.toUpperCase();
    }

    // Keine Javadoc vorhanden für diese Methode
    public void printMessage(String msg) {
        System.out.println(msg);
    }

    /**
     * Kehrt einen String um.
     *
     * @param input Der zu kehrende String.
     * @return Der umgekehrte String.
     */
    public String reverse(String input) {
        if (input == null) return null;
        return new StringBuilder(input).reverse().toString();
    }
}