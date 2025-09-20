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
import java.util.concurrent.*;

public class UDPManager {
    private final Console console;
    private final ReceivingManager receivingManager;
    private final SendingManager sendingManager;
    private int serverPort = 46525;
    private int clientPort = 4652;
    private DatagramChannel channel;
    private Selector selector;

    // Fixed thread pool для отправки запросов
    private final ExecutorService sendExecutor = Executors.newFixedThreadPool(2);

    // Cached thread pool для обработки ответов
    private final ExecutorService receiveExecutor = Executors.newCachedThreadPool();

    // ForkJoinPool для параллельной обработки
    private final ForkJoinPool responseProcessor = new ForkJoinPool();

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

    public void send(RemoteCommand object) {
        sendExecutor.submit(
                () -> {
                    sendingManager.send(object, serverPort, clientPort, channel);
                });
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
                        // Обработка в cached thread pool
                        return receiveExecutor
                                .submit(() -> receivingManager.receive(channel, clientPort))
                                .get();
                    }
                }
            }

            return new ExecutionResponse(false, "Таймаут ожидания ответа");

        } catch (Exception e) {
            return new ExecutionResponse(false, "Ошибка получения данных: " + e.getMessage());
        }
    }

    public CompletableFuture<ExecutionResponse> receiveAsync(long timeoutMillis) {
        return CompletableFuture.supplyAsync(() -> receive(timeoutMillis), responseProcessor);
    }

    public void close() {
        try {
            if (selector != null) selector.close();
            if (channel != null) channel.close();

            // Graceful shutdown пулов
            sendExecutor.shutdown();
            receiveExecutor.shutdown();
            responseProcessor.shutdown();

            if (!sendExecutor.awaitTermination(3, TimeUnit.SECONDS)) sendExecutor.shutdownNow();
            if (!receiveExecutor.awaitTermination(3, TimeUnit.SECONDS))
                receiveExecutor.shutdownNow();
            if (!responseProcessor.awaitTermination(3, TimeUnit.SECONDS))
                responseProcessor.shutdownNow();

        } catch (Exception e) {
            console.printError("Ошибка закрытия ресурсов: " + e.getMessage());
        }
    }
}
