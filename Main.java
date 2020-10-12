package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    private static TreeMap<String, Flashcard> flashcards;
    private static boolean running;
    private static ConsoleLogger logger;
    private static String autoImportFileName;
    private static String autoExportFileName;

    public Main() {
    }

    public static void main(String[] args) {
        running = true;
        flashcards = new TreeMap();
        logger = new ConsoleLogger();

        if (args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                switch (args[i]) {
                    case "-import":
                        autoImportFileName = args[i + 1];
                        break;
                    case "-export":
                        autoExportFileName = args[i + 1];
                        break;
                }
            }
        }

        if (autoImportFileName != null) {
            importCards(autoImportFileName);
        }

        while(running) {
            logger.printToConsole("Input the action (add, remove, import, export, ask, log, hardest card, reset stats, exit):");
            String option = logger.getInput();
            processAction(option);
        }

    }

    /*
     * Determines which action to take according to what the user enters
     */
    public static void processAction(String option) {
        switch (option) {
            case "add":
                addCard();
                break;
            case "remove":
                removeCard();
                break;
            case "import":
                logger.printToConsole("File name:");
                String fileName = logger.getInput();
                importCards(fileName);
                break;
            case "export":
                logger.printToConsole("File name:");
                fileName = logger.getInput();
                exportCards(fileName);
                break;
            case "ask":
                logger.printToConsole("How many times to ask?");
                quizCards(Integer.parseInt(logger.getInput()));
                break;
            case "log":
                logger.logToFile();
                break;
            case "hardest card":
                printHardestCard();
                break;
            case "reset stats":
                resetStats();
                break;
            case "exit":
                logger.printToConsole("Bye bye!");
                running = false;

                if (autoExportFileName != null) {
                    exportCards(autoExportFileName);
                }

                break;
            default:
                logger.printToConsole("This is not a valid command!");
                break;

        }
        logger.printToConsole("");
    }

    /*
     * Adds cards to the Map
     */
    public static void addCard() {
        logger.printToConsole("The card:");
        String term = logger.getInput();

        if (flashcards.containsKey(term)) { //check that term does not exist already
            logger.printToConsole(String.format("The card \"%s\" already exists.", term));
        } else { //proceed if new card
            logger.printToConsole("The definition of the card:");
            String definition = logger.getInput();

            if (checkContainsDefinition(definition)) { //check that def does not exist
                logger.printToConsole(String.format("The definition \"%s\" already exists.", definition));
            } else { //proceed with adding card
                Flashcard card = new Flashcard(term, definition);
                flashcards.put(term, card);
                logger.printToConsole(String.format("The pair (\"%s\":\"%s\") has been added.",
                        term, definition));
            }
        }
    }

    /*
     * Removes cards from the Map
     */
    public static void removeCard() {
        logger.printToConsole("The card:");
        String term = logger.getInput();
        if (flashcards.containsKey(term)) {
            flashcards.remove(term);
            logger.printToConsole("The card has been removed.");
        } else {
            logger.printToConsole(String.format("Can't remove \"%s\": there is no such card.", term));
        }
    }

    /*
     * Import cards from file
     */
    public static void importCards(String fileName) {
        File file = new File(fileName);

        try (Scanner fileScanner = new Scanner(file)) { //try with resources
            int imports = 0;

            while (fileScanner.hasNext()) { //"term : definition" added to map
                String[] pair = fileScanner.nextLine().split(" : ");
                //System.out.println(scan.nextLine().split(" : "));
                Flashcard card = new Flashcard(pair[0], pair[1]);
                if (pair.length > 2) {
                    card.setWrongAnswers(Integer.parseInt(pair[2]));
                }
                flashcards.put(pair[0], card);
                imports++;
            }
            logger.printToConsole(imports + " cards have been loaded.");
        } catch (FileNotFoundException e) {
            logger.printToConsole("File not found.");
        }
    }

    /*
     * Export cards to file
     */
    public static void exportCards(String fileName) {
        File file = new File(fileName);

        try (PrintWriter writer = new PrintWriter(file)) { //try with resources

            for (var entry : flashcards.entrySet()) { //prints as "term : definition"
                writer.println(entry.getKey() + " : " + entry.getValue().getDefinition() + " : " + entry.getValue().getWrongAnswers());
            }

            logger.printToConsole(flashcards.size() + " cards have been saved.");

        } catch (FileNotFoundException e) {
            logger.printToConsole("File not found.");
        }
    }

    /*
     * Quizzes the user on random cards as many times as specified
     */
    public static void quizCards(int times) {
        Random random = new Random();
        Object[] keys = flashcards.keySet().toArray();

        for (int i = 0; i < times; i++) { //ask # times
            int randomValue = random.nextInt(keys.length);
            String term = (String) keys[randomValue]; //get a random entry pair
            String definition = flashcards.get(term).getDefinition();
            int wrong = flashcards.get(term).getWrongAnswers();

            logger.printToConsole(String.format("Print the definition of \"%s\":", term));
            String guess = logger.getInput();
            String string = "";

            if (definition.equalsIgnoreCase(guess)) { //check answer
                string = string + "Correct answer";
            } else {
                flashcards.get(term).setWrongAnswers(++wrong);
                string = string + String.format("Wrong answer. The correct one is \"%s\"", definition);

                if (checkContainsDefinition(definition)) {
                    for (var entry : flashcards.entrySet()) {
                        if (guess.equals(entry.getValue().getDefinition())) {
                            string = string + String.format(", you've just written the definition of \"%s\".",
                                    entry.getKey());
                        }
                    }
                } else {
                    string = string + ".";
                }
            }
            logger.printToConsole(string);
        }
    }

    /*
     * Check if the definition is present in the set of flash cards
     * Used in place of map.containsValue(), as the map stores Flashcard objects as values
     */
    private static boolean checkContainsDefinition(String definition) {

        for (var entry : flashcards.entrySet()) {
            if (definition.equalsIgnoreCase(entry.getValue().getDefinition())) {
                return true;
            }
        }

        return false;
    }

    /*
     * Prints the hardest flashcards according to how many times the answer was wrong
     */
    private static void printHardestCard() {
        int mostWrong = getMostWrong();
        if (mostWrong > 0) {
            ArrayList<String> list = new ArrayList<>();
            for (var entry : flashcards.entrySet()) {
                if (entry.getValue().getWrongAnswers() == mostWrong) {
                    list.add(entry.getValue().getTerm());
                }
            }

            String string;
            if (list.size() == 1) {
                string = String.format("The hardest card is \"%s\". You have %d errors answering it.", list.get(0), mostWrong);
            } else {

                string = "The hardest cards are ";

                for (int i = 0; i < list.size(); i++) {
                    string = string + String.format("\"%s\"", list.get(i));

                    if (i == list.size() - 1) {
                        string = string + String.format(". You have %d errors answering them.", mostWrong);
                    } else {
                        string = string + ", ";
                    }
                }
            }

            logger.printToConsole(string);
        } else {
            logger.printToConsole("There are no cards with errors.");
        }
    }

    /*
     * Find out the max number of wrong answers
     */
    private static int getMostWrong() {
        int mostWrong = 0;

        for (var entry : flashcards.entrySet()) {
            mostWrong = entry.getValue().getWrongAnswers() > mostWrong ? entry.getValue().getWrongAnswers() : mostWrong;
        }

        return mostWrong;
    }

    /*
     * Reset all wrong answer values
     */
    private static void resetStats() {
        for (var entry : flashcards.entrySet()) {
            entry.getValue().setWrongAnswers(0);
        }
        logger.printToConsole("Card statistics has been reset.");
    }
}
