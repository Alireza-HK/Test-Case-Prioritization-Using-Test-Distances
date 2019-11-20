package oulu.m3s.business.similarity.impl

import oulu.m3s.business.similarity.SimilarityMetric
import oulu.m3s.utils.ShingleUtil

class JaccardDistance implements SimilarityMetric{

    //To calculate the Jaccard distance, the input text is converted to a set of k-shingles (e.g., any substring of length k found within the text). In our implementation, we used k=5 , which is commonly used in the analysis of relatively short documents.
    private static final int SHINGLE_SIZE = 5

    //The Jaccard distance, which measures dissimilarity between sample sets, is complementary to the Jaccard coefficient and is obtained by subtracting the Jaccard coefficient from 1, or, equivalently, by dividing the difference of the sizes of the union and the intersection of two sets by the size of the union.
    @Override
    public double distance(String x, String y) throws IOException {
        return 1.0 - similarity(x, y, SHINGLE_SIZE)
    }

    //The Jaccard coefficient measures similarity between finite sample sets, and is defined as the size of the intersection divided by the size of the union of the sample sets.
    private double similarity(String file1, String file2, int shingleSize) throws IOException {
        Set<String> shingles1 = ShingleUtil.shingles(file1, shingleSize);
        Set<String> shingles2 = ShingleUtil.shingles(file2, shingleSize);
        //
        int a = shingles1.size();
        int b = shingles2.size();
        shingles1.retainAll(shingles2);
        int intersect = shingles1.size();
        //
        return((double) intersect / (a + b - intersect));
    }

}
