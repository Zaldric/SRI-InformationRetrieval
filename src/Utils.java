import java.io.*;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.spanishStemmer;

import java.text.Normalizer;
import java.util.*;

class Utils {

    private Index index;
    private HashMap<String, Double> wordsFrequency;
    private Set<String> stopWords;
    private Pair<String, Integer> maxFrequency, minFrequency;

    /**
     * Creates a new Index object with:
     * <p>
     * 1- Index: the Index of the document's collection.
     * 2- StopWords: the stopWords of the spanish language.
     * 3- Max: the document with the maximum number of tokens.
     * 4- Min: the document with the minimum number of tokens.
     */
    Utils() throws IOException {

        wordsFrequency = new HashMap<>();
        stopWords = new HashSet<>();
        index = new Index();
        loadStopWords();
        maxFrequency = new Pair<>("", 0);
        minFrequency = new Pair<>("", 999999999);
    }

    /**
     * Returns a Document object with all the information of a HTML document. The text without HTML tags
     * can be obtained by using the method .text() on the returned object.
     *
     * @param path path where the document is located.
     * @return a Document object of the document.
     */
    private Document extractText(String path) throws Exception {

        FileReader reader = new FileReader(path);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return Jsoup.parse(sb.toString());
    }

    /**
     * Removes all the capital letters, special characters and accent marks of a String.
     *
     * @param text the text to be cleaned.
     * @return a String with the text cleaned of special characters.
     */
    String cleanText(String text) {

        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.toLowerCase().replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        text = text.replaceAll("[^a-z0-9-_\n]", " ");

        return text;
    }

    /**
     * Removes all HTML tags from a document and clean the resultant text of capital
     * letters, special characters and accent marks. Writing a word per line in a
     * document in the directory "results".
     *
     * @param path the path where the document is located.
     * @return the number of tokens of the document.
     */
    int extractTokens(File path) throws Exception {

        String text = extractText(path.getAbsolutePath()).text();
        text = cleanText(text);
        String words[] = text.split("\\s+");
        int numberOfTokens = 0;

        try (FileWriter fw = new FileWriter("results\\" + FilenameUtils.removeExtension(path.getName()) + ".txt")) {
            for (String word : words) {
                if (!word.equals("-")) {
                    fw.write(word + "\r\n");
                    ++numberOfTokens;
                    if (this.wordsFrequency.get(word) == null) {
                        this.wordsFrequency.put(word, 1.0);
                    } else {
                        this.wordsFrequency.replace(word, this.wordsFrequency.get(word) + 1.0);
                    }
                }
            }
        }

        if (numberOfTokens < minFrequency.getSecond()) {
            minFrequency.setSecond(numberOfTokens);
            minFrequency.setFirst(path.getName());
        }
        if (numberOfTokens > maxFrequency.getSecond()) {
            maxFrequency.setSecond(numberOfTokens);
            maxFrequency.setFirst(path.getName());
        }

        return numberOfTokens;
    }

    /**
     * Saves all the non stop wordsFrequency of a document in the index of wordsFrequency.
     *
     * @param words    an array with all the wordsFrequency of the document.
     * @param document the name of the document.
     */
    private void loadWords(String words[], String document) {

        document = FilenameUtils.removeExtension(document) + ".html";

        for (int i = 0; i < words.length; ++i) {
            if (!stopWords.contains(words[i])) {
                this.index.add(words[i], document);
            }
        }
    }

    /**
     * Load all the spanish stop wordsFrequency in a HashMap from the file "StopWords.txt" (if exist).
     */
    private void loadStopWords() throws IOException {

        if (!(new File("StopWords.txt")).exists()) {
            System.err.println("File StopWords not found");
            System.exit(1);
        }

        File file;
        FileReader fr;
        BufferedReader br;

        file = new File("StopWords.txt");
        fr = new FileReader(file);
        br = new BufferedReader(fr);
        String stopWord;
        stopWord = br.readLine();

        while (stopWord != null) {
            stopWords.add(stopWord);
            stopWord = br.readLine();
        }
    }

    /**
     * Gets all the wordsFrequency of a document and saves them in a String.
     *
     * @param path the path where the document is located.
     * @return A String array with the stem of the wordsFrequency of the document.
     */
    private String getDocumentText(File path) throws Exception {

        FileReader reader = new FileReader(path.getAbsolutePath());
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        String line;

        while ((line = br.readLine()) != null) {
            if (!stopWords.contains(line)) {
                sb.append(line);
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * Gets all the non stop wordsFrequency of a document and saves them in a String array.
     *
     * @param text the text to be cleaned.
     * @return A String array with the non stop wordsFrequency of the document.
     */
    String[] removeStopWords(String text) {

        ArrayList<String> nonStopWords = new ArrayList<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (!stopWords.contains(word)) {
                nonStopWords.add(word);
            }
        }

        String[] stockArr = new String[nonStopWords.size()];
        stockArr = nonStopWords.toArray(stockArr);

        return nonStopWords.toArray(stockArr);
    }

    /**
     * Removes all the stop wordsFrequency of a document and writes the resultant tokens in the path
     * 'stopper/document.txt'.
     *
     * @param path the path where the document is located.
     * @return the number of non stop tokens of the document.
     */
    int writeWithoutStopWords(File path) throws Exception {

        String text = getDocumentText(path);
        String[] words = removeStopWords(text);
        File log = new File("stopper\\" + path.getName());

        try (FileWriter fw = new FileWriter(log)) {
            for (int i = 0; i < words.length; ++i) {
                if (!stopWords.contains(words[i])) {
                    fw.write(words[i] + "\r\n");
                    if (this.wordsFrequency.get(words[i]) == null) {
                        this.wordsFrequency.put(words[i], 1.0);
                    } else {
                        this.wordsFrequency.replace(words[i], this.wordsFrequency.get(words[i]) + 1.0);
                    }
                }
            }
        }

        if (words.length < minFrequency.getSecond()) {
            minFrequency.setSecond(words.length);
        }
        if (words.length > maxFrequency.getSecond()) {
            maxFrequency.setSecond(words.length);
        }

        return words.length;
    }

    /**
     * Saves the highest frequency of a document.
     */
    private void setFreq(String name, String words[]) {

        index.setFrequency(FilenameUtils.removeExtension(name) + ".html", words);
    }

    /**
     * Gets all the stem wordsFrequency of the  document's tokens.
     *
     * @param words the wordsFrequency to apply the stemmer.
     */
    void stemmer(String[] words) throws Exception {

        spanishStemmer stemmer = new spanishStemmer();

        for (int i = 0; i < words.length; ++i) {
            stemmer.setCurrent(words[i]);
            if (stemmer.stem()) {
                words[i] = stemmer.getCurrent();
            }
        }
    }

    /**
     * Writes the tokens in the path 'stemmer/document.txt' after appling the stemmer in them.
     *
     * @param path the path where the document is located.
     * @return the number of tokens of the document.
     */
    int writeStemWords(File path) throws Exception {

        String text = getDocumentText(path);
        String[] words = text.split("\\s+");
        stemmer(words);
        index.addWords(FilenameUtils.removeExtension(path.getName()) + ".html", words);

        setFreq(path.getName(), words);
        loadWords(words, path.getName());
        File log = new File("stemmer\\" + path.getName());

        try (FileWriter fw = new FileWriter(log)) {
            for (int i = 0; i < words.length; ++i) {
                fw.write(words[i] + "\r\n");

                if (this.wordsFrequency.get(words[i]) == null) {
                    this.wordsFrequency.put(words[i], 1.0);

                } else {

                    this.wordsFrequency.replace(words[i], this.wordsFrequency.get(words[i]) + 1.0);
                }
            }
        }

        if (words.length < minFrequency.getSecond()) {
            minFrequency.setSecond(words.length);
        }

        if (words.length > maxFrequency.getSecond()) {
            maxFrequency.setSecond(words.length);
        }

        return words.length;
    }

    /**
     * Copy all the wordsFrequency with their frequencies in an ArrayList and sort them using quicksort.
     *
     * @return an array with all the wordsFrequency of the collection sorted highest to lowest (by frequency).
     */
    ArrayList<Pair<String, Double>> getTopWords() {

        ArrayList<Pair<String, Double>> array = new ArrayList<>();

        for (Map.Entry<String, Double> entry : wordsFrequency.entrySet()) {
            Pair<String, Double> pair = new Pair<>(entry.getKey(), entry.getValue());
            array.add(pair);
        }

        QuickSort quickSort = new QuickSort();
        quickSort.sort(array);
        wordsFrequency.clear();

        return array;
    }

    /**
     * Gets all the information about a document and saves it into the Index.
     *
     * @param path the path where the document is located.
     */
    void setDocumentInfo(File path) throws Exception {

        Document document = extractText(path.getAbsolutePath());
        DocumentInfo documentInfo = new DocumentInfo(document.title(), document.body().text());
        index.addDocument(path.getName(), documentInfo);
    }

    /**
     * @return A pair with the document with the maximum number of tokens in the collection:
     * First:   name of the document.
     * Second:  number of tokens.
     */
    Pair<String, Integer> getMaxFrequency() {
        return maxFrequency;
    }

    /**
     * @return A pair with the document with the minimum number of tokens in the collection:
     * First:   name of the document.
     * Second:  number of tokens.
     */
    Pair<String, Integer> getMinFrequency() {
        return minFrequency;
    }

    /**
     * Resets the value of the pairs for getting the maximum and minimum values of tokens in the collection.
     */
    void resetMaxMin() {

        maxFrequency.setSecond(0);
        minFrequency.setSecond(999999999);
    }

    /**
     * The structure of the index is a HashMap:
     * The HashMap contains all the different wordsFrequency in the collection and for each word there is a Pair with:
     * First: the idf value of that word.
     * Second: a HashMap with the documents that contains that word and the wn value for the word in that document.
     *
     * @return The index of the collection,
     */
    Index getIndex() {
        return index;
    }

}