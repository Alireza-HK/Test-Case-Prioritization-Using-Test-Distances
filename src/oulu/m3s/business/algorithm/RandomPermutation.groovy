package oulu.m3s.business.algorithm

class RandomPermutation {

    //Random Permutation simply randomize items within the test suite. This is basically our baseline for the comparison of various Test Case Prioritization techniques.
    public static List<String> randomize(Collection<String> originalTestSuite){
        List<String> randomlyOrdered = new ArrayList<>(originalTestSuite)
        Collections.shuffle(randomlyOrdered)
        return randomlyOrdered
    }
}
