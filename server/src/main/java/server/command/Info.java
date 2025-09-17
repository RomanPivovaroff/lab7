package server.command;

import common.command.ExecutionResponse;
import common.utility.Console;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import server.utility.CollectionManager;

/** Команда 'info'. Выводит информацию о коллекции. */
public class Info extends AbstractCommand {
    private final Console console;
    private final CollectionManager collectionManager;

    public Info(Console console, CollectionManager collectionManager) {
        super("info", "вывести информацию о коллекции");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand abstractCommand) {
        common.command.Info command = (common.command.Info) abstractCommand;

        return Stream.of(command)
                .map(
                        cmd -> {
                            LocalDateTime lastInitTime = collectionManager.getLastInitTime();
                            String lastInitTimeString =
                                    (lastInitTime == null)
                                            ? "в данной сессии инициализации еще не происходило"
                                            : lastInitTime.toLocalDate()
                                                    + " "
                                                    + lastInitTime.toLocalTime();

                            LocalDateTime lastSaveTime = collectionManager.getLastSaveTime();
                            String lastSaveTimeString =
                                    (lastSaveTime == null)
                                            ? "в данной сессии сохранения еще не происходило"
                                            : lastSaveTime.toLocalDate()
                                                    + " "
                                                    + lastSaveTime.toLocalTime();

                            String info =
                                    String.format(
                                            "Сведения о коллекции:\n"
                                                    + " Тип: %s\n"
                                                    + " Количество элементов: %d\n"
                                                    + " Дата последнего сохранения: %s\n"
                                                    + " Дата последней инициализации: %s",
                                            collectionManager.getCollection().getClass(),
                                            collectionManager.getCollection().size(),
                                            lastSaveTimeString,
                                            lastInitTimeString);

                            return new ExecutionResponse(true, info);
                        })
                .findFirst()
                .orElseGet(
                        () ->
                                new ExecutionResponse(
                                        false, "Не удалось получить информацию о коллекции"));
    }
}
