package server.command;

import common.command.ExecutionResponse;
import common.entity.Worker;
import common.utility.Console;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import server.utility.CollectionManager;

/** Команда 'remove_greater'. Удаляет из коллекции все элементы, превышающие заданный. */
public class RemoveGreater extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public RemoveGreater(Console console, CollectionManager collectionManager) {
        super(
                "remove_greater {element}",
                "удалить из коллекции все элементы, превышающие заданный");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.RemoveGreater command = (common.command.RemoveGreater) abstractCommand;
        Worker targetWorker = command.getWorker();

        if (targetWorker == null || !targetWorker.validate()) {
            return new ExecutionResponse(false, "Поля worker не валидны! Worker не создан!");
        }

        collectionManager.add(targetWorker);

        List<Worker> toRemove =
                collectionManager.getSortCollection().stream()
                        .filter(
                                w ->
                                        !Objects.equals(
                                                w,
                                                targetWorker)) // исключаем только что добавленного
                        .filter(w -> w.getSalary() > targetWorker.getSalary()) // зарплата больше
                        .filter(
                                w ->
                                        Objects.equals(
                                                w.getCreator(),
                                                command.getUsername())) // ✅ проверка создателя
                        .collect(Collectors.toList());

        toRemove.forEach(w -> collectionManager.remove(w.getId()));

        return new ExecutionResponse(
                true,
                String.format(
                        "Worker успешно добавлен, удалено %d рабочих с большей зарплатой (созданных"
                                + " вами)",
                        toRemove.size()));
    }
}
