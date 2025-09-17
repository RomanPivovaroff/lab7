package client.command;

import client.utility.CommandManager;
import common.command.AbstractCommand;
import common.command.ExecutionResponse;
import common.utility.Console;
import java.util.List;
import java.util.stream.Collectors;

/** Команда 'history'. вывести последние 7 команд (без их аргументов). */
public class History extends AbstractCommand implements Command {
    private final Console console;
    private final CommandManager commandManager;

    public History(Console console, CommandManager commandManager) {
        super("history", "вывести последние 7 команд (без их аргументов)");
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
        List<String> commandList = commandManager.getCommandHistory();
        return new ExecutionResponse(
                commandList.stream()
                        .skip(Math.max(0, commandList.size() - 7))
                        .map(command -> " " + command)
                        .collect(Collectors.joining("\n")));
    }
}
