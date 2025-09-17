package server.command;

import common.command.ExecutionResponse;
import common.entity.Worker;
import common.utility.Console;
import java.util.Objects;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/**
 * Команда 'add_if_max'. Добавляет новый элемент в коллекцию, если его значение превышает значение
 * наибольшего элемента коллекции.
 */
public class AddIfMax extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public AddIfMax(Console console, CollectionManager collectionManager) {
        super(
                "add_if_max {element}",
                "добавить новый элемент в коллекцию, если его значение превышает значение"
                        + " наибольшего элемента коллекции");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.AddIfMax command = (common.command.AddIfMax) abstractCommand;

        // Находим максимального worker по зарплате
        int maxSalary =
                collectionManager.getSortCollection().stream()
                        .map(Worker::getSalary)
                        .max(Integer::compare)
                        .orElse(Integer.MIN_VALUE);

        return Stream.of(command.getWorker())
                .filter(Objects::nonNull)
                .peek(worker -> worker.setId(collectionManager.getFreeId()))
                .filter(Worker::validate)
                .filter(worker -> worker.getSalary() > maxSalary) // проверка на max
                .findFirst()
                .map(
                        worker -> {
                            collectionManager.add(worker);
                            return new ExecutionResponse(true, "Worker успешно добавлен!");
                        })
                .orElseGet(
                        () ->
                                new ExecutionResponse(
                                        false,
                                        "Worker не максимальный или невалидный! Worker не"
                                                + " создан!"));
    }
}
