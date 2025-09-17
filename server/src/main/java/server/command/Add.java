package server.command;

import common.command.ExecutionResponse;
import common.entity.Worker;
import common.utility.Console;
import java.util.Objects;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/** Команда 'add'. Добавляет новый элемент в коллекцию. */
public class Add extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public Add(Console console, CollectionManager collectionManager) {
        super("add {element}", "добавить новый элемент в коллекцию");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду
     *
     * @return Успешность выполнения команды и сообщение об успешности.
     */
    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.Add command = (common.command.Add) abstractCommand;
        return Stream.of(command.getWorker())
                .filter(Objects::nonNull)
                .filter(Worker::validate)
                .findFirst()
                .map(
                        worker -> {
                            collectionManager.add(worker);
                            return new ExecutionResponse(true, "Worker успешно добавлен!");
                        })
                .orElseGet(
                        () ->
                                new ExecutionResponse(
                                        false, "Поля worker не валидны! Worker не создан!"));
    }
}
