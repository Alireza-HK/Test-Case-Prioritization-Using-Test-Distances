package oulu.m3s.utils

class ShingleUtil {

    private static final List<String> stopWords = new ArrayList<String>(Arrays.asList("the"));

    private static boolean isStopWord(String s) {
        if (stopWords.contains(s) || s.length() < 3) return (true);
        return (false);
    }

    public static Set<String> shingles(String content, int k){
        Set<String> output = new HashSet<>()
        output.addAll(ngrams(k, content))
        return output
    }

    private static List<String> ngrams(int n, String text) {
        String[] words = text.replaceAll("[{}().,:;']", " ").toLowerCase().split("\\s+");
        List<String> ngrams = new ArrayList<String>();
        for (int i = 0; i < words.length - n + 1; i++) {
            if (!isStopWord(words[i])){
                ngrams.add(concat(words, i, i + n));
            }
        }
        return ngrams;
    }

    private static String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
            sb.append((i > start ? " " : "") + words[i]);
        return sb.toString();
    }
}
