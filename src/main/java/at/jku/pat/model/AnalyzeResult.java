package at.jku.pat.model;

/**
 * A data record representing the aggregated metrics of a Javadoc analysis.
 * It stores raw counts for methods, documented items, and linguistic data
 * used to calculate documentation coverage and readability scores.
 *
 * @param nMethods Total number of methods identified.
 * @param nDocumentedMethods Number of methods that possess a Javadoc comment.
 * @param nItems Total number of documentable elements (parameters, return types, exceptions).
 * @param nDocumentedItems Number of documentable elements actually described in Javadoc tags.
 * @param wordCount Total number of words in the Javadoc descriptions.
 * @param sentecesCount Total number of sentences in the Javadoc descriptions.
 * @param syllablysCount Total number of syllables in the Javadoc descriptions.
 */
public record AnalyzeResult (long nMethods, long nDocumentedMethods, long nItems, long nDocumentedItems, long wordCount, long sentecesCount, long syllablysCount){
    /**
     * Calculates the "AnyJ" metric, representing the ratio of methods that have
     * at least some Javadoc documentation.
     *
     * @return A percentage (0.0 to 1.0) of documented methods, or 1.0 if no methods exist.
     */
    public double anyJ() {
        if (nMethods == 0) return 1;
        return (double)nDocumentedMethods/(double) nMethods;
    }

    /**
     * Calculates the Documentation Index Ratio (DIR), which measures the coverage
     * of specific Javadoc tags (@param, @return, @throws) relative to the
     * actual method signatures.
     *
     * @return A percentage (0.0 to 1.0) of documented items, or 1.0 if no items exist.
     */
    public double dir() {
        if (nItems == 0) return 1;
        return (double) nDocumentedItems/ (double) nItems;
    }

    /**
     * Calculates the Words Javadoc Per Declaration (WJPD) metric. This provides
     * the average word count of documentation per method.
     *
     * @return The average number of words per method.
     */
    public double wjpd(){
        if (nItems == 0) return 1;
        return (double)wordCount/(double) nMethods;
    }

    /**
     * Estimates the reading grade level of the Javadoc using the Flesch-Kincaid
     * Grade Level formula.
     *
     * @return The calculated grade level score; returns 0.0 if sentences or words are missing.
     */
    public double kincaid(){
        if (sentecesCount == 0 || wordCount == 0) {
            return 0.0;
        }

        return 0.39 * ((double) wordCount / sentecesCount)
        + 11.8 * ((double) syllablysCount / wordCount)
        - 15.59;
    }

    /**
     * Merges the metrics of this result with another {@code AnalyzeResult} instance.
     * This is typically used to aggregate results across multiple files.
     *
     * @param other The other result to combine with this one.
     * @return A new {@code AnalyzeResult} containing the summed totals of both objects.
     */
    public AnalyzeResult combine(AnalyzeResult other) {
        return new AnalyzeResult(
            this.nMethods + other.nMethods(),
                this.nDocumentedMethods + other.nDocumentedMethods(),
                this.nItems + other.nItems(),
                this.nDocumentedItems + other.nDocumentedItems(),
                this.wordCount + other.wordCount(),
                    this.sentecesCount + other.sentecesCount(),
                this.syllablysCount + other.syllablysCount()
        );
    }
}
