import java.text.Normalizer;
import java.util.*;

class Query {

    private ArrayList<Pair<String, Double>> results;
    private ArrayList<Pair<String, Double>> query;
    private Index index;

    /**
     * Creates a new Query object with:
     *
     * 1- Results: An ArrayList<Pair<String, Double>> with:
     *      First: the name of the document.
     *      Second: the similarity of that document with the query.
     * 2- Query: An ArrayList<Pair<String, Double>> with the words of the query:
     *      First: the word (stem word).
     *      Second: the word's frequency.
     * 3- Index: the Index of the documents's collection.
     */
    Query(Index index, String query) throws Exception {

        Utils util = new Utils();
        String[] words;
        this.index = index;

        query = util.cleanText(query);
        words = util.removeStopWords(query);
        util.stemmer(words);

        this.query = new ArrayList<>();
        addQueryWords(words);
        normalizeFrequencies();
        results = new ArrayList<>();
    }

    /**
     * Saves all the words of a query (without repeating) with their frequency.
     *
     * @param words the words of the query.
     */
    private void addQueryWords(String[] words) {

        boolean found;

        for (String word : words) {
            found = false;

            for (Pair<String, Double> element : query) {
                if (element.getFirst().equals(word)) {
                    element.setSecond(element.getSecond() + 1.0);
                    found = true;
                }
            }

            if (!found) {
                query.add(new Pair<>(word, 1.0));
            }
        }
    }

    /**
     * Normalizes the frequencies of the words, dividing each frequency by the maximum word's frequency and calculates
     * the weight of query's words.
     */
    private void normalizeFrequencies() {

        double max = -1;

        for (Pair<String, Double> element : query) {
            if (element.getSecond() > max) {
                max = element.getSecond();
            }
        }

        for (Pair<String, Double> element : query) {
            element.setSecond(element.getSecond() / max);
        }

        double wniqSum = 0.0;

        for (Pair<String, Double> element : query) {
            if (index.get(element.getFirst()) != null) {
                double idf = index.get(element.getFirst()).getFirst();
                double wniq = element.getSecond() * idf;
                wniqSum += wniq * wniq;
            }
        }

        wniqSum = Math.sqrt(wniqSum);

        if (wniqSum == 0) {

            for (Pair<String, Double> element : query) {
                element.setSecond(0.0);
            }

        } else {

            for (Pair<String, Double> element : query) {
                if (index.get(element.getFirst()) != null) {
                    double idf = index.get(element.getFirst()).getFirst();
                    double wniq = (element.getSecond() * idf) / wniqSum;
                    element.setSecond(wniq);
                }
            }
        }
    }

    /**
     * Calculates the similarity of the documents's collection for a query and returns an
     * ArrayList<Pair<String, Double>> sorted highest to lowest with:
     *
     *      First: the name of the document.
     *      Second: the similarity of that document with the query.
     *
     * Note: If the document dosn't contain any of the query's word, the similarity will be 0.
     *
     * @return      an array with all the relevant documents and their similarity with the query
     *              sorted highest to lowest (by frequency).
     */
    ArrayList<Pair<String, Double>> similarities() {

        if (!query.isEmpty()) {

            double wniqNorm = 0.0;
            for (Pair<String, Double> element : query) {

                if (index.get(element.getFirst()) != null) {
                        wniqNorm += element.getSecond() * element.getSecond();
                }
            }
            wniqNorm = Math.sqrt(wniqNorm);

            for (Map.Entry<String, DocumentInfo> document : index.getDocuments().entrySet()) {
                double numerator = 0.0, wnijNorm = 0.0;
                for (Pair<String, Double> element : query) {

                    if (index.get(element.getFirst()) != null) {

                        if (index.get(element.getFirst()).getSecond().get(document.getKey()) != null) {
                            double wnij = index.get(element.getFirst()).getSecond().get(document.getKey());
                            numerator += element.getSecond() * wnij;
                            wnijNorm += wnij * wnij;
                        }
                    }
                }

                wnijNorm = Math.sqrt(wnijNorm);

                if (numerator != 0.0) {
                    Double denominator = wniqNorm * wnijNorm;
                    results.add(new Pair<>(document.getKey(), numerator / denominator));
                }
            }

            QuickSort quickSort = new QuickSort();
            quickSort.sort(results);
        }

        return results;
    }

    /**
     * @return A String Array with all the words of the query (stem words).
     */
    String[] getQuery() {

        String[] words = new String[query.size()];

        for (int i = 0; i < words.length; ++i) {
            words[i] = query.get(i).getFirst();
        }

        return words;
    }

}


