package client.command;

import common.command.AbstractCommand;
import common.command.ExecutionResponse;
import common.utility.Console;

/** Команда 'execute_script'. Выполнить скрипт из файла. */
public class ExecuteScript extends AbstractCommand implements Command {
    private final Console console;
    private String filename;

    public ExecuteScript(Console console, String filename) {
        super("execute_script <file_name>", "исполнить скрипт из указанного файла");
        this.console = console;
        this.filename = filename;
    }

    public ExecuteScript(Console console) {
        super("execute_script <file_name>", "исполнить скрипт из указанного файла");
        this.console = console;
    }

    /**
     * Выполняет команду
     *
     * @return Успешность выполнения команды.
     */
    @Override
    public ExecutionResponse execute() {
        if (filename.isEmpty())
            return new ExecutionResponse(
                    false,
                    "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");

        return new ExecutionResponse("Выполнение скрипта '" + filename + "'...");
    }
}
