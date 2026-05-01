package at.jku.pat.model;

public record AnalyzeResult (long nMethods, long nDocumentedMethods){

    public double anyJ() {
        if (nMethods == 0) return 1;
        return (double)nDocumentedMethods/(double) nMethods;
    }

    public AnalyzeResult combine(AnalyzeResult other) {
        return new AnalyzeResult(
                this.nMethods + other.nMethods(),
                this.nDocumentedMethods + other.nDocumentedMethods());
    }
}
