package client.command;

import common.command.AbstractCommand;
import common.command.ExecutionResponse;
import common.utility.Console;

/** Команда 'exit'. Завершает выполнение. */
public class Exit extends AbstractCommand implements Command {
    private final Console console;

    public Exit(Console console) {
        super("exit", "завершить программу");
        this.console = console;
    }

    /**
     * Выполняет команду
     *
     * @return Успешность выполнения команды.
     */
    @Override
    public ExecutionResponse execute() {
        return new ExecutionResponse(true, "exit");
    }
}
