package server.command;

import common.command.ExecutionResponse;
import common.entity.Worker;
import common.utility.Console;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/** Команда 'update'. Обновляет элемент коллекции по ID. */
public class Update extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public Update(Console console, CollectionManager collectionManager) {
        super("update <ID> {element}", "обновить значение элемента коллекции по ID");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.Update command = (common.command.Update) abstractCommand;

        int id = command.getId();
        Worker newWorker = command.getWorker();

        if (newWorker == null || !newWorker.validate()) {
            return new ExecutionResponse(false, "Поля Рабочего не валидны! Рабочий не создан!");
        }

        Optional<ExecutionResponse> response =
                Stream.of(id)
                        .map(collectionManager::byId)
                        .filter(Objects::nonNull)
                        .filter(existing -> collectionManager.getCollection().contains(existing))
                        .map(
                                existing -> {
                                    if (!Objects.equals(
                                            existing.getCreator(), command.getUsername())) {
                                        return new ExecutionResponse(
                                                false,
                                                "Вы не являетесь создателем этого элемента!");
                                    }

                                    newWorker.setId(existing.getId());
                                    newWorker.setCreator(existing.getCreator());

                                    collectionManager.update(newWorker);
                                    return new ExecutionResponse(true, "Обновлено!");
                                })
                        .findFirst();

        return response.orElseGet(() -> new ExecutionResponse(false, "Не существующий ID"));
    }
}
