package server.command;

import common.command.ExecutionResponse;
import common.entity.Status;
import common.utility.Console;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/**
 * Команда 'print_unique_status'. Выводит уникальные значения поля status всех элементов коллекции.
 */
public class PrintUniqueStatus extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public PrintUniqueStatus(Console console, CollectionManager collectionManager) {
        super(
                "print_unique_status",
                "вывести уникальные значения поля status всех элементов в коллекции");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {

        TreeSet<Status> uniqueStatuses =
                collectionManager.getCollection().stream()
                        .map(e -> e.getStatus())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(TreeSet::new));

        boolean hasNull =
                collectionManager.getCollection().stream().anyMatch(e -> e.getStatus() == null);

        String result =
                Stream.concat(
                                hasNull ? Stream.of("null") : Stream.empty(),
                                uniqueStatuses.stream().map(Status::toString))
                        .collect(Collectors.joining(" "));

        return Stream.of(result)
                .map(s -> new ExecutionResponse(true, s))
                .findFirst()
                .orElseGet(
                        () ->
                                new ExecutionResponse(
                                        false, "Не удалось получить уникальные статусы"));
    }
}
