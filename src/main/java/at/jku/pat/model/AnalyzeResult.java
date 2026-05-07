package at.jku.pat.model;

public record AnalyzeResult (long nMethods, long nDocumentedMethods, long nItems, long nDocumentedItems, long wordCount, long sentecesCount, long syllablysCount){

    public double anyJ() {
        if (nMethods == 0) return 1;
        return (double)nDocumentedMethods/(double) nMethods;
    }

    public double dir() {
        if (nItems == 0) return 1;
        return (double) nDocumentedItems/ (double) nItems;
    }

    public double wjpd(){
        if (nItems == 0) return 1;
        return (double)wordCount/(double) nMethods;
    }

    public double kincaid(){
        if (sentecesCount == 0 || wordCount == 0) {
            return 0.0;
        }

        return 0.39 * ((double) wordCount / sentecesCount)
        + 11.8 * ((double) syllablysCount / wordCount)
        - 15.59;
    }


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
