package oulu.m3s.business.similarity;

//In statistics and related fields, a similarity measure or similarity function is a function that quantifies the similarity between two objects.
public interface SimilarityMetric {

    //This method returns an inverse value of similarity (dissimilarity) between given parameters. In practice, this can be implemented using different similarity metrics.
    double distance(String x, String y);
}
