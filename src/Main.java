import org.apache.commons.io.FilenameUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    /**
     * Returns an array with all the parameters for the application.
     * The structure of the array is:
     * Pos 0    number of parameters.
     * Pos 1    path where the collection of documents is located.
     * Pos 2    type of application (0 to load the index and 1 to search queries).
     * Pos 3    maximum number of relevant documents for a query.
     *
     * @return      the array with a parameter per position.
     */
    private static String[] loadParameters() throws IOException {

        FileReader reader = new FileReader("conf.data");
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        int number = Integer.parseInt(line);
        String[] parameters = new String[number];

        for (int i = 0; i < number; ++i) {
            parameters[i] = br.readLine();
        }

        return  parameters;
    }

    /**
     * Saves the index in the path index/classes.obj' in order to be loaded in another execution.
     *
     * @param index the object to be saved.
     */
    private static void saveIndex(Index index) throws IOException {

        ByteArrayOutputStream bs= new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream (bs);
        os.writeObject(index);
        os.close();
        byte[] bytes = bs.toByteArray();
        Path path = Paths.get("index\\classes.obj");
        Files.write(path, bytes);
    }


    public static void main(String[] args) throws Exception {

        String[] parameters = loadParameters();

        if (Integer.parseInt(parameters[2]) == 0) {

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
            File folder = new File(parameters[0]);
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
                System.out.println("Maximum tokens: " + util.getMax().getSecond() + ".");
                System.out.println("Minimum tokens: " + util.getMin().getSecond() + ".");

                System.out.println();
                System.out.println("Top 5 words before clean Stop Words: ");

                for (int i = 0; i < 5; ++i) {
                    System.out.println(topWords.get(i).getFirst() + ": " +  String.format("%.0f", topWords.get(i).getSecond()) + " times.");
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
                    System.out.println("Results directory not found");
                }

                topWords = util.getTopWords();
                System.out.println("Statistics after stopper: ");
                System.out.println("Total number of tokens: " + sum + ".");
                System.out.println("Token average per file: " + sum / listOfFiles.length + ".");
                System.out.println("Maximum tokens: " + util.getMax().getSecond() + ".");
                System.out.println("Minimum tokens: " + util.getMin().getSecond() + ".");

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
                    System.out.println("Stopper directory not found");
                }

                topWords = util.getTopWords();
                System.out.println("Statistics after stemmer: ");
                System.out.println("Total number of tokens: " + sum + ".");
                System.out.println("Token average per file: " + sum / listOfFiles.length + ".");
                System.out.println("Maximum tokens: " + util.getMax().getSecond() + ".");
                System.out.println("Minimum tokens: " + util.getMin().getSecond() + ".");

                System.out.println();
                System.out.println("Top 5 words after stemmer: ");

                for (int i = 0; i < 5; ++i) {
                    System.out.println(topWords.get(i).getFirst() + ": " + String.format("%.0f", topWords.get(i).getSecond()) + " times.");
                }

                System.out.println();
                System.out.println("Statistics after Index: ");
                System.out.println(util.getIndex().getTotalDocuments() + " documents processed.");
                System.out.println("There are " + util.getIndex().getNumberOfWords() + " different tokens.");
                System.out.println("The most extensive document is '" + util.getMax().getFirst() + "' with " + util.getMax().getSecond() + " words.");
                System.out.println("The less extensive document is '" + util.getMin().getFirst() + "' with " + util.getMin().getSecond() + " words.");

                System.out.println();
                System.out.println("Normalizing .... ");
                util.getIndex().normalizeFrequencies();
                System.out.println("Done.");
                System.out.println("Saving index .... ");
                saveIndex(util.getIndex());
                System.out.println("Done.");

                time_end = System.currentTimeMillis();
                System.out.println("The program has finished in " + (float) (time_end - time_start) / 1000.0 + " seconds.");
            } else {
                System.out.println("No files in such directory");
            }

        } else {

            System.out.println("Hola");
        }

    }
}
