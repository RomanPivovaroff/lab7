package server.command;

import common.command.ExecutionResponse;
import common.utility.Console;
import server.utility.CollectionManager;

/** Команда 'clear'. Очищает коллекцию. */
public class Clear extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public Clear(Console console, CollectionManager collectionManager) {
        super("clear", "очистить коллекцию");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду
     *
     * @return Успешность выполнения команды.
     */
    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.Clear command = (common.command.Clear) abstractCommand;

        String username = command.getUsername();

        var toRemove =
                collectionManager.getCollection().stream()
                        .filter(worker -> worker.getCreator() != null)
                        .filter(worker -> worker.getCreator().equals(username))
                        .toList();

        toRemove.forEach(worker -> collectionManager.remove(worker.getId()));

        return new ExecutionResponse(
                true,
                String.format(
                        "Удалено %d элементов, созданных пользователем %s",
                        toRemove.size(), username));
    }
}
