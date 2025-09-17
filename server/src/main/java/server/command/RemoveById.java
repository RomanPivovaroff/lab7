package server.command;

import common.command.ExecutionResponse;
import common.utility.Console;
import java.util.Optional;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/** Команда 'remove_by_id'. Удаляет элемент из коллекции по ID. */
public class RemoveById extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public RemoveById(Console console, CollectionManager collectionManager) {
        super("remove_by_id <ID>", "удалить элемент из коллекции по ID");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.RemoveById command = (common.command.RemoveById) abstractCommand;

        Optional<ExecutionResponse> response =
                Stream.of(command.getId())
                        .map(collectionManager::byId)
                        .filter(
                                item ->
                                        item != null
                                                && collectionManager.getCollection().contains(item))
                        .map(
                                item -> {
                                    if (!command.getUsername().equals(item.getCreator())) {
                                        return new ExecutionResponse(
                                                false,
                                                "Вы не являетесь создателем этого элемента!");
                                    }
                                    collectionManager.remove(item.getId());
                                    return new ExecutionResponse(true, "Рабочий успешно удалён!");
                                })
                        .findFirst();

        return response.orElseGet(() -> new ExecutionResponse(false, "Несуществующий ID"));
    }
}
