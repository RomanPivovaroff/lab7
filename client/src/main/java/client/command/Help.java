package client.command;

import client.utility.CommandManager;
import common.command.AbstractCommand;
import common.command.ExecutionResponse;
import common.utility.Console;
import java.util.stream.Collectors;

/** Команда 'help'. Выводит справку по доступным командам */
public class Help extends AbstractCommand implements Command {
    private final Console console;
    private final CommandManager commandManager;

    public Help(Console console, CommandManager commandManager) {
        super("help", "вывести справку по доступным командам");
        this.console = console;
        this.commandManager = commandManager;
    }

    /**
     * Выполняет команду
     *
     * @return Успешность выполнения команды.
     */
    @Override
    public ExecutionResponse execute() {

        return new ExecutionResponse(
                commandManager.getCommands().values().stream()
                        .map(
                                command ->
                                        String.format(
                                                " %-35s%-1s%n",
                                                command.getName(), command.getDescription()))
                        .collect(Collectors.joining("\n")));
    }
}
