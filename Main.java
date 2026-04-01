import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Random;

//NAME: Anas Ali
//ASSIGNMENT: Program 3
//LAB SECTION: Week 7
//LECTURE SECTION: 12

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<SentList> sentList = new ArrayList<SentList>();
        ArrayList<SentList> posList = new ArrayList<SentList>();
        ArrayList<SentList> negList = new ArrayList<SentList>();
        ArrayList<Words> wordList = new ArrayList<Words>();

        // load sentiment, positive words and negative words arraylists
        readSentimentList(sentList, posList, negList);

        // read review
        // load ArrayList wordList that will contain original review & pos & neg
        String inFileName;
        String outFileName = "reviews.txt";
        PrintWriter outFile = new PrintWriter(outFileName);

        // open input file adding review + number + ".txt" to review
        // if not able to open, print a message and continue
        // else process the file
        // if the file can be read properly, print the results
        for (int i = 1; i <= 8; i++) {

            if (i == 5) {
                inFileName = "review5a.txt";
            } else {
                inFileName = "review" + i + ".txt";
            }

            wordList.clear();

            boolean success = readReview(sentList, posList, negList, wordList, inFileName);

            if (!success) {
                System.out.println("Could not open file: " + inFileName);
            } else {
                printReview(wordList, inFileName, outFile);
            }
        }

        outFile.close();
    }

    // PRE: accept the empty ArrayLists created in main
    // POST: the arrays are loaded with the proper words and information
    public static void readSentimentList(ArrayList<SentList> sentList,
            ArrayList<SentList> posList,
            ArrayList<SentList> negList) {
        String csvFilePath = "sentiment.txt";
        String line;
        double tempValue;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while ((line = br.readLine()) != null) {

                // Split the line by commas into an array of strings
                String[] values = line.split(",");

                if (values.length < 2)
                    continue;

                String word = values[0];

                try {
                    tempValue = Double.parseDouble(values[1]);
                } catch (NumberFormatException e) {
                    continue;
                }

                // Create object SentList & add to sentList arraylist
                SentList temp = new SentList(word, tempValue);
                sentList.add(temp);

                // if word values are pos add to posList
                if (tempValue > 1.25) {
                    posList.add(temp);
                }

                // if word values are neg, add to neglist
                if (tempValue < -1.25) {
                    negList.add(temp);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the file.");
            e.printStackTrace();
        }
    }

    // PRE: accept the word lists and file name to open
    // POST: read the file while the line is not null
    // each word is edited (to lower case without punctuation)
    // the sentiment value is accessed
    // if the word is positive - update to a random word in the negative list and
    // update the word value
    // if the word is negative - update to a random positive word in the positive
    // list & update the word value

    public static boolean readReview(ArrayList<SentList> sentList,
            ArrayList<SentList> posList,
            ArrayList<SentList> negList,
            ArrayList<Words> wordList,
            String fileName) {

        String line;
        Random rand = new Random();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {

                String[] values = line.split(" ");

                for (int i = 0; i < values.length; i++) {

                    String origWord = values[i];
                    String editWord = origWord.toLowerCase();

                    if (editWord.length() > 0) {
                        char last = editWord.charAt(editWord.length() - 1);
                        if (isPunctuation(last)) {
                            editWord = editWord.substring(0, editWord.length() - 1);
                        }
                    }

                    double origValue = getSentiment(sentList, editWord);

                    String posWord = origWord;
                    String negWord = origWord;

                    double posValue = origValue;
                    double negValue = origValue;

                    if (origValue < -1) {
                        int r = rand.nextInt(posList.size());
                        SentList newWord = posList.get(r);
                        posWord = newWord.word;
                        posValue = newWord.value;
                    }

                    if (origValue > 1) {
                        int r = rand.nextInt(negList.size());
                        SentList newWord = negList.get(r);
                        negWord = newWord.word;
                        negValue = newWord.value;
                    }

                    Words tempWord = new Words(origWord, editWord, posWord, negWord,
                            origValue, posValue, negValue);

                    wordList.add(tempWord);
                }
            }

            return true;

        } catch (IOException e) {
            System.out.println("Error reading the file: " + fileName);
            return false;
        }
    }

    // PRE: accept the updated wordlist
    // POST: loop through word list, create a string that will be the original,
    // positive & negative reviews
    // print each review
    public static void printReview(ArrayList<Words> wordList, String inFile,
            PrintWriter outFile) {

        outFile.println("Original Review for File: " + inFile);
        outFile.println();

        String original = "";
        String positive = "";
        String negative = "";

        double origTotal = 0;
        double posTotal = 0;
        double negTotal = 0;

        for (Words w : wordList) {

            original += w.origWord + " ";
            positive += w.posWord + " ";
            negative += w.negWord + " ";

            origTotal += w.sentOrigValue;
            posTotal += w.sentPosValue;
            negTotal += w.sentNegValue;
        }

        printFormatted(original, outFile);

        outFile.println();
        outFile.println("Original Sentiment: " + origTotal);
        outFile.println();

        outFile.println();
        outFile.println("Positive Review for File: " + inFile);
        printFormatted(positive, outFile);
        outFile.println();
        outFile.println("Positive Sentiment: " + posTotal);

        outFile.println();
        outFile.println("Negative Review for File: " + inFile);
        printFormatted(negative, outFile);
        outFile.println();
        outFile.println("Negative Sentiment: " + negTotal);

        outFile.println();
        outFile.println();
    }

    static void printFormatted(String text, PrintWriter outFile) {

        int index = 0;

        while (index < text.length()) {
            int end = Math.min(index + 80, text.length());
            outFile.println(text.substring(index, end));
            index = end;
        }
    }

    // PRE: accept a character
    // POST: return true if this character is punctuation; false otherwise
    // There may be a better way to write this!
    static boolean isPunctuation(char ch) {
        if (ch == '!' || ch == '"' || ch == '#' || ch == '$' || ch == '%' || ch == '&' || ch == '\''
                || ch == '(' || ch == ')' || ch == '*' || ch == '+' || ch == ',' || ch == '-'
                || ch == '.' || ch == '/' || ch == ':' || ch == ';' || ch == '<' || ch == '='
                || ch == '>' || ch == '?' || ch == '@' || ch == '[' || ch == '\\'
                || ch == ']' || ch == '^' || ch == '`' || ch == '{' || ch == '|'
                || ch == '}')
            return true;
        return false;
    }

    // PRE: accept the sentiment words list and a word to find
    // POST: return the value of the sentiment if found, 0 otherwise
    static double getSentiment(ArrayList<SentList> sentList, String eWord) {

        for (SentList s : sentList) {
            if (s.word.equals(eWord)) {
                return s.value;
            }
        }

        return 0.0;
    }
}