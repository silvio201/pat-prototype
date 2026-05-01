package at.jku.pat.model;

public record AnalyzeResult (long nMethods, long nDocumentedMethods, long nItems, long nDocumentedItems){

    public double anyJ() {
        if (nMethods == 0) return 1;
        return (double)nDocumentedMethods/(double) nMethods;
    }

    public double dir() {
        if (nMethods == 0) return 1;
        return (double) nDocumentedItems/ (double) nItems;
    }

    public AnalyzeResult combine(AnalyzeResult other) {
        return new AnalyzeResult(
                this.nMethods + other.nMethods(),
                this.nDocumentedMethods + other.nDocumentedMethods(),
                this.nItems + other.nItems(),
                this.nDocumentedItems + other.nDocumentedItems()
        );
    }
}
