import java.io.*;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.spanishStemmer;
import java.text.Normalizer;
import java.util.*;

class Utils {

    private Index index;
    private HashMap<String, Double> words;
    private HashMap<String, Integer> stopWords;
    private Pair<String, Integer> max, min;

    /**
     * Creates a new Index object with:
     *
     * 1- Index: the Index of the document's collection.
     * 2- StopWords: the stopWords of the spanish language.
     * 3- Max: the document with the maximum number of tokens.
     * 4- Min: the document with the minimum number of tokens.
     */
    Utils() throws IOException {

        words = new HashMap<>();
        stopWords = new HashMap<>();
        index = new Index();
        loadStopWords();
        max = new Pair<>("", 0);
        min = new Pair<>("", 999999999);
    }

    /**
     * Returns a Document object with all the information of a HTML document. The text without HTML tags
     * can be obtained by using the method .text() on the returned object.
     *
     * @param path  path where the document is located.
     * @return      a Document object of the document.
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
     * @param text  the text to be cleaned.
     * @return      a String with the text cleaned of special characters.
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
     * @param path  the path where the document is located.
     * @return      the number of tokens of the document.
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
                    if (this.words.get(word) == null) {
                        this.words.put(word, 1.0);
                    } else {
                        this.words.replace(word, this.words.get(word) + 1.0);
                    }
                }
            }
        }

        if (numberOfTokens < min.getSecond()) {
            min.setSecond(numberOfTokens);
            min.setFirst(path.getName());
        }
        if (numberOfTokens > max.getSecond()) {
            max.setSecond(numberOfTokens);
            max.setFirst(path.getName());
        }

        return numberOfTokens;
    }

    /**
     * Saves all the non stop words of a document in the index of words.
     *
     * @param words  an array with all the words of the document.
     * @param document  the name of the document.
     */
    private void loadWords(String words[], String document) {

        document = FilenameUtils.removeExtension(document) + ".html";

        for (int i = 0; i < words.length; ++i) {
            if (stopWords.get(words[i]) == null) {
                this.index.add(words[i], document);
            }
        }
    }

    /**
     * Load all the spanish stop words in a HashMap from the file "StopWords.txt" (if exist).
     */
    private void loadStopWords() throws IOException {

        if (!(new File("StopWords.txt")).exists()) {
            System.out.println("File StopWords not found");
        }

        File file;
        FileReader fr;
        BufferedReader br;

        file = new File("StopWords.txt");
        fr = new FileReader(file);
        br = new BufferedReader(fr);
        String text;
        text = br.readLine();

        while (text != null) {
            stopWords.put(text, 0);
            text = br.readLine();
        }
    }

    /**
     * Gets all the words of a document and saves them in a String.
     *
     * @param path  the path where the document is located.
     * @return      A String array with the stem of the words of the document.
     */
    private String getDocumentText(File path) throws  Exception {

        FileReader reader = new FileReader(path.getAbsolutePath());
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        String line;

        while ((line = br.readLine()) != null) {
            if (this.stopWords.get(line) == null) {
                sb.append(line);
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * Gets all the non stop words of a document and saves them in a String array.
     *
     * @param text  the text to be cleaned.
     * @return      A String array with the non stop words of the document.
     */
    String[] removeStopWords(String text) {

        ArrayList<String> nonStopWords = new ArrayList<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (this.stopWords.get(word) == null) {
                nonStopWords.add(word);
            }
        }

        String[] stockArr = new String[nonStopWords.size()];
        stockArr = nonStopWords.toArray(stockArr);

        return nonStopWords.toArray(stockArr);
    }

    /**
     * Removes all the stop words of a document and writes the resultant tokens in the path
     * 'stopper/document.txt'.
     *
     * @param path  the path where the document is located.
     * @return      the number of non stop tokens of the document.
     */
    int writeWithoutStopWords(File path) throws Exception {

        String text = getDocumentText(path);
        String[] words = removeStopWords(text);
        File log = new File("stopper\\" + path.getName());

        try (FileWriter fw = new FileWriter(log)) {
            for (int i = 0; i < words.length; ++i) {
                if (this.stopWords.get(words[i]) == null) {
                    fw.write(words[i] + "\r\n");
                    if (this.words.get(words[i]) == null) {
                        this.words.put(words[i], 1.0);
                    } else {
                        this.words.replace(words[i], this.words.get(words[i]) + 1.0);
                    }
                }
            }
        }

        if (words.length < min.getSecond()) {
            min.setSecond(words.length);
        }
        if (words.length > max.getSecond()) {
            max.setSecond(words.length);
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
     * Gets all the stem words of the  document's tokens.
     *
     * @param words  the words to apply the stemmer.
     */
    void stemmer(String[] words) throws  Exception {

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
     * @param path  the path where the document is located.
     * @return      the number of tokens of the document.
     */
    int writeStemWords(File path) throws Exception {

        String text = getDocumentText(path);
        String[] words = text.split("\\s+");
        stemmer(words);

        setFreq(path.getName(), words);
        loadWords(words, path.getName());
        File log = new File("stemmer\\" + path.getName());

        try (FileWriter fw = new FileWriter(log)) {
            for (int i = 0; i < words.length; ++i) {
                fw.write(words[i] + "\r\n");

                if (this.words.get(words[i]) == null) {
                    this.words.put(words[i], 1.0);

                } else {

                    this.words.replace(words[i], this.words.get(words[i]) + 1.0);
                }
            }
        }

        if (words.length < min.getSecond()) {
            min.setSecond(words.length);
        }

        if (words.length > max.getSecond()) {
            max.setSecond(words.length);
        }

        return words.length;
    }

    /**
     * Copy all the words with their frequencies in an ArrayList and sort them using quicksort.
     *
     * @return      an array with all the words of the collection sorted highest to lowest (by frequency).
     */
    ArrayList<Pair<String, Double>> getTopWords() {

        ArrayList<Pair<String, Double>> array = new ArrayList<>();

        for (Map.Entry<String, Double> entry : words.entrySet()) {
            Pair<String, Double> pair = new Pair<>(entry.getKey(), entry.getValue());
            array.add(pair);
        }

        QuickSort quickSort = new QuickSort();
        quickSort.sort(array);
        words.clear();

        return array;
    }

    /**
     * Gets all the information about a document and saves it into the Index.
     *
     * @param path  the path where the document is located.
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
    Pair<String, Integer> getMax() { return max;}

    /**
     * @return A pair with the document with the minimum number of tokens in the collection:
     * First:   name of the document.
     * Second:  number of tokens.
     */
    Pair<String, Integer> getMin() { return min;}

    /**
     * Resets the value of the pairs for getting the maximum and minimum values of tokens in the collection.
     */
    void resetMaxMin() {

        max.setSecond(0);
        min.setSecond(999999999);
    }

    /**
     * The structure of the index is a HashMap:
     * The HashMap contains all the different words in the collection and for each word there is a Pair with:
     * First: the idf value of that word.
     * Second: a HashMap with the documents that contains that word and the wn value for the word in that document.
     *
     * @return The index of the collection,
     */
    Index getIndex() {return index;}

}