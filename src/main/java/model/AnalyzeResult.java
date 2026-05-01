package model;

public record AnalyzeResult (long nMethods, long nDocumentedMethods){

    public double anyJ() {
        return (double)nMethods/(double) nDocumentedMethods;
    }

    public AnalyzeResult combine(AnalyzeResult other) {
        return new AnalyzeResult(
                this.nMethods + other.nMethods(),
                this.nDocumentedMethods + other.nDocumentedMethods());
    }
}
