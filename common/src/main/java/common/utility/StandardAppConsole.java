package common.utility;

import java.util.NoSuchElementException;
import java.util.Scanner;

/** Для ввода команд и вывода результата */
public class StandardAppConsole implements Console {
    private static final String P = "$ ";
    private static Scanner fileScanner = null;
    private static final Scanner defScanner = new Scanner(System.in);
    private static boolean scriptMode = false;

    /**
     * Выводит obj.toString() в консоль
     *
     * @param obj Объект для печати
     */
    public void print(Object obj) {
        if (!scriptMode) System.out.print(obj);
    }

    /**
     * Выводит obj.toString() + \n в консоль
     *
     * @param obj Объект для печати
     */
    public void println(Object obj) {
        if (!scriptMode) System.out.println(obj);
    }

    @Override
    public void scriptPrintMode(boolean mode) {
        scriptMode = mode;
    }

    /**
     * Выводит ошибка: obj.toString() в консоль
     *
     * @param obj Ошибка для печати
     */
    public void printError(Object obj) {
        System.err.println("Error: " + obj);
    }

    public String readln() throws NoSuchElementException, IllegalStateException {
        return (fileScanner != null ? fileScanner : defScanner).nextLine();
    }

    public boolean isCanReadln() throws IllegalStateException {
        return (fileScanner != null ? fileScanner : defScanner).hasNextLine();
    }

    /** Выводит prompt текущей консоли */
    public void prompt() {
        print(P);
    }

    /**
     * @return prompt текущей консоли
     */
    public String getPrompt() {
        return P;
    }

    public void selectFileScanner(Scanner scanner) {
        this.fileScanner = scanner;
    }

    public void selectConsoleScanner() {
        this.fileScanner = null;
    }
}
