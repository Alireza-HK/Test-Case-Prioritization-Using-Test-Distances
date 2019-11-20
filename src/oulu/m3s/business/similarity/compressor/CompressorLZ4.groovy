package oulu.m3s.business.similarity.compressor

import net.jpountz.lz4.LZ4Compressor
import net.jpountz.lz4.LZ4Factory

class CompressorLZ4 implements Compressor {

    @Override
    int compress(String inputString) {
        //LZ4 is a fast and lossless compression algorithm -> http://lz4.github.io/lz4/
        LZ4Factory factory = LZ4Factory.fastestInstance();
        byte[] data = inputString.getBytes("UTF-8");
        final int decompressedLength = data.length;
        // compress data
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        //return the length of the input string after its compression. This is a proxy for measuring Kolmogorov complexity
        int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
        return compressedLength
    }
}
