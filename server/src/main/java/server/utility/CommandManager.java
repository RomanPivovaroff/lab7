package server.utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import server.command.AbstractCommand;

/** Управляет командами. */
public class CommandManager {
    private static final Map<String, AbstractCommand> commands = new LinkedHashMap<>();
    private final List<String> commandHistory = new ArrayList<>();

    /**
     * Добавляет команду.
     *
     * @param commandName Название команды.
     * @param command Команда.
     */
    public void register(String commandName, AbstractCommand command) {
        commands.put(commandName, command);
    }

    /**
     * @return Словарь команд.
     */
    public Map<String, AbstractCommand> getCommands() {
        return commands;
    }

    /**
     * @return История команд.
     */
    public List<String> getCommandHistory() {
        return commandHistory;
    }

    /**
     * Добавляет команду в историю.
     *
     * @param command Команда.
     */
    public void addToHistory(String command) {
        commandHistory.add(command);
    }
}
