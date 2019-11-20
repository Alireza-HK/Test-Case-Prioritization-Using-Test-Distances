package oulu.m3s.business.similarity.compressor;

public interface Compressor {

    //return the length of the input string after its compression. This is a proxy for measuring Kolmogorov complexity
    public int compress(String inputString);
}
