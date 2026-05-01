package at.jku.pat.model;

public record AnalyzeResult (long nMethods, long nDocumentedMethods, long nItems, long nDocumentedItems, long wordCount){

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


    public AnalyzeResult combine(AnalyzeResult other) {
        return new AnalyzeResult(
                this.nMethods + other.nMethods(),
                this.nDocumentedMethods + other.nDocumentedMethods(),
                this.nItems + other.nItems(),
                this.nDocumentedItems + other.nDocumentedItems(),
                this.wordCount + other.wordCount()
        );
    }
}
