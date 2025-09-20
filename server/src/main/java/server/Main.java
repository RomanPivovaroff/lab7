package server;

import common.utility.StandardAppConsole;
import java.io.IOException;
import server.command.*;
import server.dbmanagers.DBManager;
import server.network.ReceivingManager;
import server.network.SendingManager;
import server.network.UDPManager;
import server.utility.*;

public class Main {
    public static void main(String[] args) {
        StandardAppConsole console = new StandardAppConsole();
        String scriptFileName = null;
        if (args.length == 1) {
            scriptFileName = args[0];
        }
        if (new DBManager().CreateTable(scriptFileName)) console.println("скрипт выполнен учпешно");
        UDPManager udpManager = null;
        int port = -1;
        do {
            console.println("Введите порт(число от 0 до 65535): ");
            String line = console.readln();
            try {
                if (!line.isBlank()) port = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                console.printError("некоректный порт");
            }
        } while (port > 65535 || port < 0);
        console.print("Сервер открыт на порту: " + port);
        CollectionManager collectionManager = new CollectionManager();
        // Регистрируем хук для экстренного завершения программы
        Terminate terminateHook = new Terminate(console, collectionManager, udpManager);
        Runtime.getRuntime().addShutdownHook(terminateHook);
        // продолжаем логику программы
        var commandManager =
                new CommandManager() {
                    {
                        register("info", new Info(console, collectionManager));
                        register("show", new Show(console, collectionManager));
                        register("add", new Add(console, collectionManager));
                        register("update", new Update(console, collectionManager));
                        register("remove_by_id", new RemoveById(console, collectionManager));
                        register("clear", new Clear(console, collectionManager));
                        register("add_if_max", new AddIfMax(console, collectionManager));
                        register("remove_greater", new RemoveGreater(console, collectionManager));
                        register(
                                "filter_by_organization",
                                new FilterByOrganization(console, collectionManager));
                        register("print_ascending", new PrintAscending(console, collectionManager));
                        register(
                                "print_unique_status",
                                new PrintUniqueStatus(console, collectionManager));
                        register("login", new Login());
                        register("register", new Register());
                    }
                };
        try {
            udpManager =
                    new UDPManager(
                            port, new SendingManager(), new ReceivingManager(), commandManager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    ;
}
