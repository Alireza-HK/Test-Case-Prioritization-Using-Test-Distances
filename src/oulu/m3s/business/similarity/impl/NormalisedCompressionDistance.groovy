package oulu.m3s.business.similarity.impl

import oulu.m3s.business.similarity.SimilarityMetric
import oulu.m3s.business.similarity.compressor.Compressor
import oulu.m3s.business.similarity.compressor.CompressorLZ4

class NormalisedCompressionDistance implements SimilarityMetric{

    private Compressor compressor;

    NormalisedCompressionDistance() {
        this.compressor = new CompressorLZ4();
    }

    NormalisedCompressionDistance(Compressor compressor) {
        this.compressor = compressor
    }

    /*
    Two objects are deemed similar if we can significantly compress one given the information in the other. NCD relies on a compressor function C that calculates the approximate Kolmogorov complexity and returns the length of the input string after its compression, using a chosen compression program.
    Reference: Cilibrasi, Rudi, and Paul MB Vitányi. "Clustering by compression." IEEE Transactions on Information theory 51.4 (2005): 1523-1545.
     */
    @Override
    public double distance(String x, String y) {
        int cx = compressor.compress(x);
        int cy = compressor.compress(y);
        int cxy = compressor.compress(x + y);
        return (cxy - (double) Math.min(cx, cy)) / Math.max(cx, cy);
    }

}
