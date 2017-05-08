import java.io.Serializable;
import java.util.*;

class Index implements Serializable {

    private HashMap<String, Pair<Double, HashMap<String, Double>>> words;
    private HashMap<String, DocumentInfo> documents;
    private HashMap<String, Integer> frequencies;

    /**
     * Creates a new Index object with:
     * <p>
     * 1- Words: all the stem words of the documents's collection.
     * 2- Documents: the information of the documents's collection.
     * 3- Frequiencies: the maximun frequency of a word for each document.
     */
    Index() {

        words = new HashMap<>();
        frequencies = new HashMap<>();
        documents = new HashMap<>();
    }

    /**
     * Inserts a word in the index of words.
     * <p>
     * The structure of the index is a HashMap:
     * The HashMap contains all the different words in the collection and for each word there is a Pair with:
     * <p>
     * First: the idf value of that word.
     * Second: a HashMap with the documents that contains that word and the wn value for the word in that document
     * (before calculating the weights, it contains the word frequency in that document).
     *
     * @param word     the word that will be loaded in the index.
     * @param document the document that contains the word.
     */
    void add(String word, String document) {

        if (words.get(word) == null) {
            HashMap<String, Double> ocurrences;
            ocurrences = new HashMap<>();
            ocurrences.put(document, 1.0);
            Pair<Double, HashMap<String, Double>> pair = new Pair<>(null, ocurrences);
            words.put(word, pair);

        } else {
            if (words.get(word).getSecond().get(document) == null) {
                words.get(word).getSecond().put(document, 1.0);

            } else {
                words.get(word).getSecond().replace(document, words.get(word).getSecond().get(document) + 1);
            }
        }
    }

    /**
     * Gets the word with the highest frequency of the document and saves that frequency in a HashMap.
     *
     * @param name  the name of the document.
     * @param words array with all the words of that document.
     */
    void setFrequency(String name, String[] words) {

        HashMap<String, Integer> wordFreq = new HashMap<>();

        for (int i = 0; i < words.length; ++i) {
            if (wordFreq.get(words[i]) == null) {
                wordFreq.put(words[i], 1);

            } else {
                wordFreq.replace(words[i], wordFreq.get(words[i]) + 1);
            }
        }

        int freq = -1;

        for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
            if (entry.getValue() > freq) {
                freq = entry.getValue();
            }
        }

        frequencies.put(name, freq);
    }

    /**
     * Calculates the weights of the index's words.
     * <p>
     * Calculating for each word:
     * 1- The IDF value for that word.
     * 2- The WN value for that word in each document that contains that word.
     */
    void calculateWeights() {

        words.remove("");

        for (Map.Entry<String, Pair<Double, HashMap<String, Double>>> entry : words.entrySet()) {
            double sum = 0;
            double log = Math.log10((double) frequencies.size() / entry.getValue().getSecond().size());
            words.get(entry.getKey()).setFirst(log);

            for (Map.Entry<String, Double> entry2 : words.get(entry.getKey()).getSecond().entrySet()) {
                int maxFrequency = frequencies.get(entry2.getKey());
                words.get(entry.getKey()).getSecond().replace(entry2.getKey(), entry2.getValue() / maxFrequency); //Weight normalization
                double wij = log * words.get(entry.getKey()).getSecond().get(entry2.getKey());
                words.get(entry.getKey()).getSecond().replace(entry2.getKey(), wij);
                sum += (wij * wij);
            }

            sum = Math.sqrt(sum);

            for (Map.Entry<String, Double> entry2 : words.get(entry.getKey()).getSecond().entrySet()) {
                if (sum == 0.0) {
                    words.get(entry.getKey()).getSecond().replace(entry2.getKey(), 0.0);
                } else {
                    words.get(entry.getKey()).getSecond().replace(entry2.getKey(), entry2.getValue() / sum);
                }
            }
        }
    }

    /**
     * Adds all the documents's information to the Index with all the relevant information about it.
     * <p>
     * The documentInfo structure is:
     * 1- Title: title of the document.
     * 2- Phrases: The sentences of the document separated by '.'.
     * 3- CleanPhrases: The sentences of the document cleaned.
     *
     * @param document     the name of the document.
     * @param documentInfo object with all the information about the document.
     */
    void addDocument(String document, DocumentInfo documentInfo) {
        documents.put(document, documentInfo);
    }

    /**
     * Adds the document's words to the index.
     *
     * @param document the name of the document.
     * @param words    the document's words.
     */
    void addWords(String document, String[] words) {
        documents.get(document).setWordsFrequency(words);
    }

    /**
     * @return The Index of words of the document collection.
     */
    HashMap<String, Pair<Double, HashMap<String, Double>>> getIndex() {
        return words;
    }

    /**
     * Returns a Pair with the information of the word in the collection.
     * The structure of the Pair is:
     * First: the idf value of that word.
     * Second: a HashMap with the documents that contains that word and the wn value for the word in that document.
     *
     * @param key the word to search in the Index.
     * @return A pair with the information of that word.
     */
    Pair<Double, HashMap<String, Double>> get(String key) {
        return words.get(key);
    }

    /**
     * @return The number of documents that has the collection.
     */
    int getTotalDocuments() {
        return frequencies.size();
    }

    /**
     * @return The number of different words that has the collection of documents.
     */
    int getNumberOfWords() {
        return words.size();
    }

    String getTopWords(String document, int number, ArrayList<String> usedWords) {
        return documents.get(document).getTopWords(number, usedWords);
    }

    /**
     * @return A HashMap with all the documents's collection.
     */
    HashMap<String, DocumentInfo> getDocuments() {
        return documents;
    }
}
