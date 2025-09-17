package client;

import client.command.*;
import client.utility.CommandManager;
import client.utility.ReceivingManager;
import client.utility.Runner;
import client.utility.SendingManager;
import client.utility.UDPManager;
import common.command.*;
import common.utility.StandardAppConsole;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    private static int port;
    private static String server_adress;

    public static void main(String[] args) {
        int clientPort = 10000 + (int) (Math.random() * 40001);
        var console = new StandardAppConsole();
        int port = -1;
        do {
            console.println("Введите порт(число от 0 до 65535): ");
            String line = console.readln();
            try {
                if (!line.isBlank()) port = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                port = -1;
                console.printError("некоректный порт");
            }
        } while (port > 65535 || port < 0);
        InetAddress serverAddress = null;
        do {
            console.println("Введите адрес сервера(например: 192.168.10.80");
            String line = console.readln();
            try {
                if (!line.isBlank()) serverAddress = InetAddress.getByName(line);
            } catch (UnknownHostException e) {
                console.printError("некоректный адресс");
            }
        } while (serverAddress == null);
        var udpManager =
                new UDPManager(
                        console,
                        new ReceivingManager(console),
                        new SendingManager(console, serverAddress),
                        port,
                        clientPort);

        var commandManager =
                new CommandManager(console, udpManager) {
                    {
                        register("help", new Help(console, this));
                        register("execute_script", new ExecuteScript(console));
                        register("exit", new Exit(console));
                        register("info", new Info());
                        register("show", new Show());
                        register("add", new Add());
                        register("help", new Help(console, this));
                        register("update", new Update());
                        register("remove_by_id", new RemoveById());
                        register("clear", new Clear());
                        register("exit", new Exit(console));
                        register("add_if_max", new AddIfMax());
                        register("remove_greater", new RemoveGreater());
                        register("history", new History(console, this));
                        register("filter_by_organization", new FilterByOrganization());
                        register("print_ascending", new PrintAscending());
                        register("print_unique_status", new PrintUniqueStatus());
                        register("register", new Register());
                        register("login", new Login());
                    }
                };

        new Runner(console, commandManager, udpManager).interactiveMode();
    }
}
