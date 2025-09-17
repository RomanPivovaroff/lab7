package server.command;

import common.command.ExecutionResponse;
import common.utility.Console;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/** Команда 'show'. Выводит все элементы коллекции. */
public class Show extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public Show(Console console, CollectionManager collectionManager) {
        super(
                "show",
                "вывести в стандартный поток вывода все элементы коллекции в строковом"
                        + " представлении");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.Show command = (common.command.Show) abstractCommand;

        String result =
                collectionManager.getCollection().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n"));

        return Stream.of(result)
                .map(s -> new ExecutionResponse(true, s))
                .findFirst()
                .orElseGet(() -> new ExecutionResponse(false, "Коллекция пуста"));
    }
}
