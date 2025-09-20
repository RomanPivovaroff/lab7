package client.utility;

import client.command.*;
import common.command.*;
import common.exceptions.TooMuchArgumentsException;
import common.utility.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/** Класс для запуска команд */
public class Runner {
    private final Console console;
    private CommandManager invoker;
    private final List<String> scriptStack = new ArrayList<>();
    private int lengthRecursion = -1;

    public Runner(Console console, CommandManager commandManager, UDPManager udpManager) {
        this.console = console;
        this.invoker = commandManager;
    }

    /** Интерактивный режим */
    public void interactiveMode() {
        try {
            ExecutionResponse commandStatus = new ExecutionResponse(false, "");
            String[] userCommand = {"", ""};

            while (true) {
                console.prompt();
                userCommand = (console.readln().trim() + " last").split(" ");
                userCommand[1] = userCommand[1].trim();
                try {
                    commandStatus = launchCommand(userCommand);
                } catch (TooMuchArgumentsException e) {
                    console.printError(e.getMessage());
                }

                if (commandStatus.getMassage().equals("exit")) break;
                console.println(commandStatus.getMassage());
            }
        } catch (NoSuchElementException exception) {
            console.printError("Пользовательский ввод не обнаружен!");
        } catch (IllegalStateException exception) {
            console.printError("Непредвиденная ошибка!");
        }
    }

    /**
     * Проверяет рекурсивность выполнения скриптов.
     *
     * @param argument Название запускаемого скрипта
     * @return можно ли выполнять скрипт.
     */
    private boolean checkRecursion(String argument, Scanner scriptScanner) {
        var recStart = -1;
        var i = 0;
        for (String script : scriptStack) {
            i++;
            if (argument.equals(script)) {
                if (recStart < 0) recStart = i;
                if (lengthRecursion < 0) {
                    console.selectConsoleScanner();
                    console.println(
                            "Была замечена рекурсия! Введите максимальную глубину рекурсии"
                                    + " (0..500)");
                    while (lengthRecursion < 0 || lengthRecursion > 500) {
                        try {
                            console.print("> ");
                            lengthRecursion = Integer.parseInt(console.readln().trim());
                        } catch (NumberFormatException e) {
                            console.println("длина не распознана");
                        }
                    }
                    console.selectFileScanner(scriptScanner);
                }
                if (i > recStart + lengthRecursion || i > 500) return false;
            }
        }
        return true;
    }

    /**
     * Режим для запуска скрипта.
     *
     * @param argument Аргумент скрипта
     * @return Код завершения + сообщение.
     */
    private ExecutionResponse scriptMode(String argument) {
        String[] userCommand = {"", ""};
        StringBuilder executionOutput = new StringBuilder();

        if (!new File(argument).exists()) return new ExecutionResponse(false, "Файл не существет!");
        if (!Files.isReadable(Paths.get(argument)))
            return new ExecutionResponse(false, "Прав для чтения нет!");

        scriptStack.add(argument);
        try (Scanner scriptScanner = new Scanner(new File(argument))) {

            ExecutionResponse commandStatus;

            if (!scriptScanner.hasNext()) throw new NoSuchElementException();
            console.selectFileScanner(scriptScanner);
            do {
                userCommand = (console.readln().trim() + " ").split(" ", 2);
                userCommand[1] = userCommand[1].trim();
                while (console.isCanReadln() && userCommand[0].isEmpty()) {
                    userCommand = (console.readln().trim() + " ").split(" ", 2);
                    userCommand[1] = userCommand[1].trim();
                }
                executionOutput.append(console.getPrompt() + String.join(" ", userCommand) + "\n");
                var needLaunch = true;
                if (userCommand[0].equals("execute_script")) {
                    needLaunch = checkRecursion(userCommand[1], scriptScanner);
                }
                switch (userCommand[0]) {
                    case "add",
                                    "add_if_max",
                                    "remove_greater",
                                    "filter_by_organization",
                                    "update" ->
                            console.scriptPrintMode(true);
                }
                commandStatus =
                        needLaunch
                                ? launchCommand(userCommand)
                                : new ExecutionResponse(
                                        true, "Превышена максимальная глубина рекурсии");
                console.scriptPrintMode(false);
                if (needLaunch && userCommand[0].equals("execute_script"))
                    console.selectFileScanner(scriptScanner);
                executionOutput.append(commandStatus.getMassage() + "\n");
                if (commandStatus.getMassage().equals("exit")) {
                    console.println(executionOutput.toString());
                    System.exit(0);
                }
            } while (commandStatus.getIsSucceeded() && console.isCanReadln());

            console.selectConsoleScanner();
            if (!commandStatus.getIsSucceeded()
                    && !(userCommand[0].equals("execute_script") && !userCommand[1].isEmpty())) {
                executionOutput.append("Проверьте скрипт на корректность введенных данных!\n");
            }

            return new ExecutionResponse(
                    commandStatus.getIsSucceeded(), executionOutput.toString());
        } catch (FileNotFoundException exception) {
            return new ExecutionResponse(false, "Файл со скриптом не найден!");
        } catch (NoSuchElementException exception) {
            return new ExecutionResponse(false, "Файл со скриптом пуст!");
        } catch (IllegalStateException exception) {
            console.printError("Непредвиденная ошибка!");
            System.exit(0);
        } catch (TooMuchArgumentsException e) {
            throw new RuntimeException(e);
        } finally {
            scriptStack.remove(scriptStack.size() - 1);
        }
        console.scriptPrintMode(false);
        return new ExecutionResponse("");
    }

    /**
     * Launchs the command.
     *
     * @param userCommand Команда для запуска
     * @return Код завершения + сообщение.
     */
    private ExecutionResponse launchCommand(String[] userCommand) throws TooMuchArgumentsException {
        if (userCommand[0].isEmpty()) return new ExecutionResponse(false, "");

        invoker.addToHistory(userCommand[0]);
        switch (userCommand[0].toUpperCase()) {
            case "EXECUTE_SCRIPT" -> {
                if (userCommand.length != 3)
                    throw new TooMuchArgumentsException(1, userCommand.length - 1);
                ExecutionResponse tmp =
                        invoker.invoke((Command) new ExecuteScript(console, userCommand[1]));
                if (!tmp.getIsSucceeded()) return tmp;
                console.println(tmp.getMassage());
                ExecutionResponse tmp2 = scriptMode(userCommand[1]);
                return new ExecutionResponse(tmp2.getIsSucceeded(), tmp2.getMassage().trim());
            }
            case "ADD" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                try {
                    return invoker.invoke(
                            new Add(
                                    Ask.AskWorker(
                                            console, Math.abs(UUID.randomUUID().hashCode()) + 1)));
                } catch (Ask.AskBreak e) {
                    return new ExecutionResponse(false, "отмена создания рабочего");
                }
            }
            case "ADD_IF_MAX" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                try {
                    return invoker.invoke(
                            new AddIfMax(
                                    Ask.AskWorker(
                                            console, Math.abs(UUID.randomUUID().hashCode()) + 1)));
                } catch (Ask.AskBreak e) {
                    return new ExecutionResponse(false, "отмена создания рабочего");
                }
            }
            case "REMOVE_GREATER" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                try {
                    return invoker.invoke(
                            new RemoveGreater(
                                    Ask.AskWorker(
                                            console, Math.abs(UUID.randomUUID().hashCode()) + 1)));
                } catch (Ask.AskBreak e) {
                    return new ExecutionResponse(false, "отмена создания рабочего");
                }
            }
            case "CLEAR" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke(new Clear());
            }
            case "EXIT" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke((Command) new Exit(console));
            }
            case "HELP" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke((Command) new Help(console, invoker));
            }
            case "INFO" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke(new Info());
            }
            case "HISTORY" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke((Command) new History(console, invoker));
            }
            case "PRINT_ASCENDING" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke(new PrintAscending());
            }
            case "PRINT_UNIQUE_STATUS" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke(new PrintUniqueStatus());
            }
            case "REMOVE_BY_ID" -> {
                if (userCommand.length != 3)
                    throw new TooMuchArgumentsException(1, userCommand.length - 1);
                return invoker.invoke(new RemoveById(Integer.parseInt(userCommand[1])));
            }
            case "FILTER_BY_ORGANIZATION" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                try {
                    return invoker.invoke(new FilterByOrganization(Ask.askOrganization(console)));
                } catch (Ask.AskBreak e) {
                    return new ExecutionResponse(false, "отмена создания организации");
                }
            }
            case "SHOW" -> {
                if (userCommand.length != 2)
                    throw new TooMuchArgumentsException(0, userCommand.length - 1);
                return invoker.invoke(new Show());
            }
            case "UPDATE" -> {
                if (userCommand.length != 3)
                    throw new TooMuchArgumentsException(1, userCommand.length - 1);
                int id = Integer.parseInt(userCommand[1]);
                try {
                    return invoker.invoke(
                            new Update(
                                    Ask.AskWorker(
                                            console, Math.abs(UUID.randomUUID().hashCode()) + 1),
                                    id));
                } catch (Ask.AskBreak e) {
                    return new ExecutionResponse(false, "отмена создания рабочего");
                }
            }
            case "LOGIN" -> {
                if (userCommand.length != 4)
                    throw new TooMuchArgumentsException(2, userCommand.length - 1);
                return invoker.invoke(new Login(userCommand[1], userCommand[2]));
            }
            case "REGISTER" -> {
                console.println(userCommand.length);
                if (userCommand.length != 4)
                    throw new TooMuchArgumentsException(2, userCommand.length - 1);
                return invoker.invoke(new Register(userCommand[1], userCommand[2]));
            }
            default -> {
                return new ExecutionResponse(
                        false,
                        "Команда '" + userCommand[0] + "' не найдена. Наберите 'help' для справки");
            }
        }
    }
}
