package client.utility;

import common.command.ExecutionResponse;
import common.command.RemoteCommand;
import common.utility.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class UDPManager {
    private final Console console;
    private final ReceivingManager receivingManager;
    private final SendingManager sendingManager;
    private int serverPort = 46525;
    private int clientPort = 4652;
    private DatagramChannel channel;
    private Selector selector;
    private int maxWorkerCount = 490;

    public UDPManager(
            Console console,
            ReceivingManager receivingManager,
            SendingManager sendingManager,
            int serverPort,
            int clientPort) {
        this.console = console;
        this.receivingManager = receivingManager;
        this.sendingManager = sendingManager;
        this.serverPort = serverPort;
        this.clientPort = clientPort;

        try {
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(clientPort));
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            console.printError("Не удалось создать сетевой канал: " + e.getMessage());
        }
    }

    public int getMaxWorkerCount() {
        return maxWorkerCount;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void send(RemoteCommand object) {
        sendingManager.send(object, serverPort, clientPort, channel);
    }

    public ExecutionResponse receive(long timeoutMillis) {
        try {
            if (!selector.isOpen() || !channel.isOpen()) {
                return new ExecutionResponse(false, "Сетевое соединение закрыто");
            }

            if (selector.select(timeoutMillis) > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isReadable()) {
                        ExecutionResponse response = receivingManager.receive(channel, clientPort);
                        if (response != null) {
                            return response;
                        } else {
                            return new ExecutionResponse(
                                    false, "Не удалось обработать ответ сервера");
                        }
                    }
                }
            }

            // Таймаут без получения данных
            return new ExecutionResponse(
                    false, "Таймаут ожидания ответа от сервера (" + timeoutMillis + "ms)");

        } catch (IOException e) {
            return new ExecutionResponse(
                    false, "Ошибка сети при получении данных: " + e.getMessage());
        } catch (Exception e) {
            return new ExecutionResponse(
                    false, "Неожиданная ошибка при получении данных: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            console.printError("Ошибка закрытия ресурсов: " + e.getMessage());
        }
    }

    /** Проверяет, открыто ли соединение */
    public boolean isConnected() {
        return channel != null && channel.isOpen() && selector != null && selector.isOpen();
    }

    /** Переподключается к серверу */
    public ExecutionResponse reconnect() {
        close();

        try {
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(clientPort));
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            return new ExecutionResponse(true, "Переподключение выполнено успешно");
        } catch (IOException e) {
            return new ExecutionResponse(false, "Ошибка переподключения: " + e.getMessage());
        }
    }
}
