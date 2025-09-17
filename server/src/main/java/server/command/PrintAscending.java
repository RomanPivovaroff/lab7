package server.command;

import common.command.ExecutionResponse;
import common.entity.Worker;
import common.utility.Console;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/** Команда 'print_ascending'. Выводит элементы коллекции в порядке возрастания. */
public class PrintAscending extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public PrintAscending(Console console, CollectionManager collectionManager) {
        super("print_ascending", "вывести элементы коллекции в порядке возрастания");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {

        String result =
                collectionManager.getSortCollection().stream()
                        .map(Worker::toString)
                        .collect(Collectors.joining("\n"));

        return Stream.of(result)
                .map(s -> new ExecutionResponse(true, s))
                .findFirst()
                .orElseGet(
                        () -> new ExecutionResponse(false, "Коллекция пуста или произошла ошибка"));
    }
}
