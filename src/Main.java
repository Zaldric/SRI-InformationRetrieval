import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static private String collectionPath, indexPath;
    static private int programMode, relevantDocumentsQuery, relevantDocumentsPSR, topWordsPSR;

    /**
     * Returns an array with all the parameters for the application.
     * The structure of the array is:
     * Pos 0    number of parameters.
     * Pos 1    path where the collection of documents is located.
     * Pos 2    type of application (0 to load the index and 1 to search queries).
     * Pos 3    maximum number of relevant documents for a query.
     *
     * @return the array with a parameter per position.
     */
    private static void loadParameters() throws IOException {

        /*
        FileReader reader = new FileReader("conf.data");
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        int number = Integer.parseInt(line);
        String[] parameters = new String[number];

        for (int i = 0; i < number; ++i) {
            parameters[i] = br.readLine();
        }

        return  parameters;
        */

        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();

        try {

            Object obj = parser.parse(new FileReader("confData.json"));

            JSONObject jsonObject = (JSONObject) obj;

            collectionPath = (String) jsonObject.get("ColectionPath");
            indexPath = (String) jsonObject.get("IndexPath");
            programMode = Integer.parseInt(jsonObject.get("ProgramMode").toString());
            relevantDocumentsQuery = Integer.parseInt(jsonObject.get("RelevantDocumentsQuery").toString());
            relevantDocumentsPSR = Integer.parseInt(jsonObject.get("RelevantDocumentsPSR").toString());
            topWordsPSR = Integer.parseInt(jsonObject.get("TopWordsPSR").toString());

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Saves the index in the path index/classes.obj' in order to be loaded in another execution.
     *
     * @param index the object to be saved.
     */
    private static void saveIndex(Index index) throws IOException {

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        os.writeObject(index);
        os.close();
        byte[] bytes = bs.toByteArray();
        Path path = Paths.get("index\\Index");
        Files.write(path, bytes);
    }

    /**
     * Loads the Index of the document's collection from the path 'index/classes.obj' and returns it.
     *
     * @param path the path where the Index.obj is located.
     * @return the Index of the document's collection.
     */
    private static Index loadIndex(String path) throws IOException {

        Index index = null;

        try {

            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            index = (Index) ois.readObject();
            ois.close();
            fis.close();

        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
            System.exit(1);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return index;
    }

    /**
     * Prints all the relevant info of the most relevants documents for a query (if any).
     *
     * @param top          the top of relevants documents for the given query.
     * @param index        the Index of the documents's collection.
     * @param query        the query to search.
     * @param maxDocuments the maximum of relevants documents that will be returned.
     */
    private static void printQueriesInfo(ArrayList<Pair<String, Double>> top, Index index, String[] query, int maxDocuments) {

        DocumentInfo document;

        if (!top.isEmpty()) {

            System.out.println();

            for (int i = 0; i < top.size() && i < maxDocuments; ++i) {
                document = index.getDocuments().get(top.get(i).getFirst());
                System.out.println("Number: " + (i + 1) + ".");
                System.out.println("Name: '" + top.get(i).getFirst() + "'.");
                System.out.println("Similarity: " + top.get(i).getSecond() + ".");
                System.out.println("Title: " + document.getTitle() + ".");
                System.out.println("Text: " + document.searchFullQuery(query) + ".");
                System.out.println();
            }

        } else {
            System.out.println("");
            System.out.println("No relevant documents found.");
        }
    }

    public static void main(String[] args) throws Exception {

        loadParameters();

        if (programMode == 0) {

            File file = new File("results");
            file.mkdir();
            File file2 = new File("stopper");
            file2.mkdir();
            File file3 = new File("stemmer");
            file3.mkdir();
            File file4 = new File("index");
            file4.mkdir();

            Utils util = new Utils();
            int sum = 0;
            ArrayList<Pair<String, Double>> topWords;
            File folder = new File(collectionPath);
            File[] listOfFiles = folder.listFiles();

            long time_start, time_end;
            time_start = System.currentTimeMillis();

            if (listOfFiles != null) {

                for (File listOfFile : listOfFiles) {
                    sum += util.extractTokens(listOfFile);
                    util.setDocumentInfo(listOfFile);
                }

                topWords = util.getTopWords();
                System.out.println(listOfFiles.length + " files processed.");
                System.out.println("Total number of tokens: " + sum + ".");
                System.out.println("Token average per file: " + sum / listOfFiles.length + ".");
                System.out.println("Maximum tokens: " + util.getMaxFrequency().getSecond() + ".");
                System.out.println("Minimum tokens: " + util.getMinFrequency().getSecond() + ".");

                System.out.println();
                System.out.println("Top 5 words before clean Stop Words: ");

                for (int i = 0; i < 5; ++i) {
                    System.out.println(topWords.get(i).getFirst() + ": " + String.format("%.0f", topWords.get(i).getSecond()) + " times.");
                }

                util.resetMaxMin();
                System.out.println();
                File folder2 = new File("results");
                File[] listOfFiles2 = folder2.listFiles();
                sum = 0;

                if (listOfFiles2 != null) {
                    for (File aListOfFiles2 : listOfFiles2) {
                        sum += util.writeWithoutStopWords(aListOfFiles2);
                    }
                } else {
                    System.err.println("Results directory not found");
                    System.exit(1);
                }

                topWords = util.getTopWords();
                System.out.println("Statistics after stopper: ");
                System.out.println("Total number of tokens: " + sum + ".");
                System.out.println("Token average per file: " + sum / listOfFiles.length + ".");
                System.out.println("Maximum tokens: " + util.getMaxFrequency().getSecond() + ".");
                System.out.println("Minimum tokens: " + util.getMinFrequency().getSecond() + ".");

                System.out.println();
                System.out.println("Top 5 words after clean Stop Words: ");

                for (int i = 0; i < 5; ++i) {
                    System.out.println(topWords.get(i).getFirst() + ": " + String.format("%.0f", topWords.get(i).getSecond()) + " times.");
                }

                System.out.println();
                File folder3 = new File("stopper");
                File[] listOfFiles3 = folder3.listFiles();
                sum = 0;
                util.resetMaxMin();

                if (listOfFiles3 != null) {
                    for (File aListOfFiles3 : listOfFiles3) {
                        sum += util.writeStemWords(aListOfFiles3);
                    }
                } else {
                    System.err.println("Stopper directory not found");
                    System.exit(1);
                }

                topWords = util.getTopWords();
                System.out.println("Statistics after stemmer: ");
                System.out.println("Total number of tokens: " + sum + ".");
                System.out.println("Token average per file: " + sum / listOfFiles.length + ".");
                System.out.println("Maximum tokens: " + util.getMaxFrequency().getSecond() + ".");
                System.out.println("Minimum tokens: " + util.getMinFrequency().getSecond() + ".");

                System.out.println();
                System.out.println("Top 5 words after stemmer: ");

                for (int i = 0; i < 5; ++i) {
                    System.out.println(topWords.get(i).getFirst() + ": " + String.format("%.0f", topWords.get(i).getSecond()) + " times.");
                }

                System.out.println();
                System.out.println("Statistics after Index: ");
                System.out.println(util.getIndex().getTotalDocuments() + " documents processed.");
                System.out.println("There are " + util.getIndex().getNumberOfWords() + " different tokens.");
                System.out.println("The most extensive document is '" + util.getMaxFrequency().getFirst() + "' with " + util.getMaxFrequency().getSecond() + " words.");
                System.out.println("The less extensive document is '" + util.getMinFrequency().getFirst() + "' with " + util.getMinFrequency().getSecond() + " words.");

                System.out.println();
                System.out.println("Normalizing .... ");
                util.getIndex().calculateWeights();
                System.out.println("Done.");
                System.out.println("Saving index .... ");
                saveIndex(util.getIndex());
                System.out.println("Done.");

                time_end = System.currentTimeMillis();
                System.out.println("The program has finished in " + (float) (time_end - time_start) / 1000.0 + " seconds.");
            } else {
                System.err.println("No documents in such directory");
                System.exit(1);
            }

        } else {

            long time_start, time_end;
            time_start = System.currentTimeMillis();

            System.out.println("Loading Index...");
            Index index = loadIndex(indexPath);
            System.out.println("Done.");
            System.out.println();
            String line;
            Scanner scan = new Scanner(System.in);

            System.out.println("Enter your query: ");
            line = scan.nextLine();

            do {

                Query query = new Query(index, line);
                ArrayList<Pair<String, Double>> top = query.similarities();

                printQueriesInfo(top, index, query.getQuery(), relevantDocumentsQuery);

                if (!top.isEmpty()) {

                    System.out.println("-------------------------------------------------------------------");
                    System.out.println("Relevant documents after appling PSR: ");
                    System.out.println("-------------------------------------------------------------------");
                    StringBuilder sb = new StringBuilder();
                    sb.append(line);
                    sb.append(" ");

                    for (int i = 0; i < relevantDocumentsPSR; ++i) {

                        sb.append(index.getTopWords(top.get(i).getFirst(), topWordsPSR));
                    }

                    Query query2 = new Query(index, sb.toString());
                    top = query2.similarities();

                    printQueriesInfo(top, index, query2.getQuery(), relevantDocumentsQuery);
                }

                System.out.println();
                System.out.println("Enter your query: ");
                line = scan.nextLine();

            } while (!line.equals("exit"));

            time_end = System.currentTimeMillis();
            System.out.println("The program has finished in " + (float) (time_end - time_start) / 1000.0 + " seconds.");
        }

    }
}
