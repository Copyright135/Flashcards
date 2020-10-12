package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleLogger {

    private static ArrayList<String> log;

    public ConsoleLogger() {
        this.log = new ArrayList<>();
    }

    public static void printToConsole(String string) {
        System.out.println(string);
        log.add(string);
    }

    public static String getInput() {
        Scanner scan = new Scanner(System.in);
        String string = scan.nextLine();
        log.add(string);
        return(string);
    }

    public static void logToFile() {
        printToConsole("File name:");
        String fileName = getInput();
        File file = new File(fileName);

        try (PrintWriter writer = new PrintWriter(file)) {

            for (var line : log) {
                writer.println(line);
            }

            printToConsole("The log has been saved.");

        } catch (FileNotFoundException e) {
            printToConsole("File not found");
        }
    }
}
