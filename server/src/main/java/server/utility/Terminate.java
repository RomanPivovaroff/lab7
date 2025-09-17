package server.utility;

import common.utility.Console;
import server.network.UDPManager;

/** Класс для корректного завершения программы при падении */
public class Terminate extends Thread {
    private final Console console;
    private final CollectionManager collectionManager;
    private final UDPManager udpManager;

    public Terminate(Console console, CollectionManager collectionManager, UDPManager udpManager) {
        this.console = console;
        this.collectionManager = collectionManager;
        this.udpManager = udpManager;
    }

    /**
     * Выполняет действия при завершении программы Выводит сообщение о завершении и записывает
     * данные в backupcollection.xml
     */
    public void run() {
        console.println("Завершение программы");
        udpManager.stop();
    }
}
