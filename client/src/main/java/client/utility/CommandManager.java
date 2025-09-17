package client.utility;

import client.command.Command;
import common.command.*;
import common.utility.Console;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Управляет командами. */
public class CommandManager {
    private static final Map<String, AbstractCommand> commands = new LinkedHashMap<>();
    private final List<String> commandHistory = new ArrayList<>();
    private Console console;
    private UDPManager udpManager;
    private String username = "";
    private String password = "";

    public CommandManager(Console console, UDPManager udpManager) {
        this.udpManager = udpManager;
        this.console = console;
    }

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

    public ExecutionResponse invoke(RemoteCommand cmd) {
        if (!(cmd instanceof LoginCommand)) {
            ((AbstractCommand) cmd).setUsername(username);
            ((AbstractCommand) cmd).setPassword(password);
            if (cmd instanceof Add || cmd instanceof RemoveGreater || cmd instanceof AddIfMax) {
                if (cmd instanceof Add) {
                    ((Add) cmd).getWorker().setCreator(username);
                } else if (cmd instanceof RemoveGreater) {
                    ((RemoveGreater) cmd).getWorker().setCreator(username);
                } else {
                    ((AddIfMax) cmd).getWorker().setCreator(username);
                }
            }
        }
        udpManager.send((AbstractCommand) cmd);
        ExecutionResponse response = udpManager.receive(10000);
        if (cmd instanceof LoginCommand) {

            if (response.getMassage().trim().equals("Вы успешно вошли в аккаунт.")
                    || response.getMassage().trim().equals("Вы успешно зарегитрировались.")) {
                this.username = ((AbstractCommand) cmd).getUsername();
                this.password = ((AbstractCommand) cmd).getPassword();
            }
        }
        return response;
    }

    public ExecutionResponse invoke(Command cmd) {
        return cmd.execute();
    }
}
