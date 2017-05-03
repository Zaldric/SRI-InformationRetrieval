import org.tartarus.snowball.ext.spanishStemmer;

import java.io.IOException;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class DocumentInfo implements Serializable {

    private String title;
    private ArrayList<String> phrases;
    private ArrayList<String> cleanPhrases;
    private ArrayList<Pair<String, Double>> wordsFrequency;

    /**
     * Creates a new DocumentInfo object with:
     * <p>
     * 1- Title: title of the document.
     * 2- Phrases: The sentences of the document separated by '.'.
     * 3- CleanPhrases: The sentences of the document cleaned.
     *
     * @param title        title of the document.
     * @param originalBody body of the document (without HTML tags).
     */
    DocumentInfo(String title, String originalBody) throws Exception {

        this.title = title;
        phrases = new ArrayList<>();
        cleanPhrases = new ArrayList<>();
        wordsFrequency = new ArrayList<>();
        setPhrases(originalBody);
    }

    /**
     * Separates the body of the document into sentences separated by '.' and saves them in the phrases
     * ArrayList. Then cleans each phrase (remove special characters, stop words and stemming)
     * and saves it into the cleanPhrases ArrayList.
     *
     * @param originalBody body of the document (without HTML tags).
     */
    private void setPhrases(String originalBody) throws Exception {

        String[] phrases = originalBody.split("\\.");
        this.phrases.add(title);
        this.phrases.addAll(Arrays.asList(phrases));
        Utils util = new Utils();

        String phrase;

        for (int i = 0; i < this.phrases.size(); ++i) {

            phrase = util.cleanText(this.phrases.get(i));
            String[] words = util.removeStopWords(phrase);
            util.stemmer(words);
            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < words.length; ++j) {
                sb.append(words[j]);
                sb.append(" ");
            }

            cleanPhrases.add(sb.toString());
        }
    }

    /**
     * Searches the words of a query in the document and returns a String with the first phrase that contains
     * some of the words of the query or an empty String if there is no such sentence.
     *
     * @param words the words to search in the document.
     * @return a String with the first phrase that contains some of the words.
     */
    String searchFullQuery(String[] words) {

        for (int i = 0; i < cleanPhrases.size(); ++i) {
            String[] phraseWords = cleanPhrases.get(i).split("\\s+");

            for (int j = 0; j < words.length; ++j) {

                for (int k = 0; k < phraseWords.length; ++k) {
                    if (phraseWords[k].equals(words[j])) {

                        return phrases.get(i);
                    }
                }
            }
        }

        return "";
    }

    /**
     * Gets all the document's words and their frequencies into the wordsFrequency array and sort it
     * highest to lowest using quicksort.
     *
     * @param words the document's words.
     */
    void setWordsFrequency(String[] words) {

        HashMap<String, Double> wordsFrequency = new HashMap<>();

        for (String word : words) {

            if (wordsFrequency.get(word) == null) {
                wordsFrequency.put(word, 0.0);
            } else {
                wordsFrequency.replace(word, wordsFrequency.get(word) + 1.0);
            }
        }

        for (Map.Entry<String, Double> entry : wordsFrequency.entrySet()) {
            this.wordsFrequency.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        QuickSort quickSort = new QuickSort();
        quickSort.sort(this.wordsFrequency);
    }

    /**
     * Gets a number of top words of the document.
     *
     * @param number the number of top words to get from the document.
     */
    String getTopWords(int number) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < number && i < wordsFrequency.size(); ++i) {
            sb.append(wordsFrequency.get(i).getFirst());
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * @return The title of the document.
     */
    String getTitle() {
        return title;
    }

}