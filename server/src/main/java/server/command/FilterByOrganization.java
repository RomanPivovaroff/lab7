package server.command;

import common.command.ExecutionResponse;
import common.entity.Organization;
import common.entity.Worker;
import common.utility.Console;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/** Команда 'filter_by_organization'. Выводит элементы с заданной организацией. */
public class FilterByOrganization extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public FilterByOrganization(Console console, CollectionManager collectionManager) {
        super(
                "filter_by_organization {organization}",
                "вывести элементы, значение поля organization которых равно заданному");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.FilterByOrganization command =
                (common.command.FilterByOrganization) abstractCommand;
        Organization targetOrg = command.getOrganization(); // Организация передана в команду

        return Stream.of(collectionManager.getCollection())
                .flatMap(col -> col.stream())
                .filter(worker -> Objects.equals(worker.getOrganization(), targetOrg))
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list ->
                                        list.isEmpty()
                                                ? new ExecutionResponse(
                                                        true,
                                                        "Не найден соответствующий Organization")
                                                : new ExecutionResponse(
                                                        true,
                                                        list.stream()
                                                                        .map(Worker::toString)
                                                                        .collect(
                                                                                Collectors.joining(
                                                                                        "\n"))
                                                                + "\n"
                                                                + "Все элементы с заданной"
                                                                + " организацией выведены!")));
    }
}
