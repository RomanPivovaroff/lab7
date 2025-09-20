package server.network;

import common.command.ExecutionResponse;
import common.command.Register;
import common.utility.ProgramStatus;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.command.AbstractCommand;
import server.utility.CommandManager;

public class UDPManager {
    private int port = 46525;
    private final ReceivingManager receivingManager;
    private final SendingManager sendingManager;
    private final CommandManager commandManager;
    private HashSet<InetSocketAddress> sessions = new HashSet<>();
    private static final Logger logger = Logger.getLogger(UDPManager.class.getName());

    private final ExecutorService requestReader = Executors.newFixedThreadPool(5);

    private final ExecutorService requestProcessor = Executors.newCachedThreadPool();

    private final ForkJoinPool responseSender = new ForkJoinPool();

    private volatile boolean isRunning = false;

    public UDPManager(
            int port,
            SendingManager sendingManager,
            ReceivingManager receivingManager,
            CommandManager commandManager)
            throws IOException {
        this.port = port;
        this.sendingManager = sendingManager;
        this.receivingManager = receivingManager;
        this.commandManager = commandManager;

        this.receivingManager.initialize(port);
        this.sendingManager.initialize();

        isRunning = true;
        logger.info("UDPManager запущен с многопоточной обработкой");

        startProcessingThreads();
    }

    private void startProcessingThreads() {
        // Запуск Fixed thread pool для чтения
        for (int i = 0; i < 5; i++) {
            requestReader.submit(this::readRequests);
        }

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            requestProcessor.submit(this::processRequests);
        }

        responseSender.submit(this::sendResponses);
    }

    private void readRequests() {
        while (isRunning) {
            try {
                Object receivedObject = receivingManager.receive();
                if (receivedObject != null) {
                    // Передача на обработку в cached pool
                    requestProcessor.submit(() -> processReceivedObject(receivedObject));
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка чтения запросов", e);
            }
        }
    }

    private void processRequests() {
        while (isRunning) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void sendResponses() {
        while (isRunning) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processReceivedObject(Object object) {
        if (object instanceof ProgramStatus) {
            handleProgramStatus((ProgramStatus) object);
        } else if (object instanceof common.command.AbstractCommand) {
            handleRemoteCommand((common.command.AbstractCommand) object);
        } else {
            logger.warning(
                    "Получен неизвестный тип объекта: "
                            + (object != null ? object.getClass().getName() : "null"));
        }
    }

    private void handleRemoteCommand(common.command.AbstractCommand command) {
        InetSocketAddress clientAddress = receivingManager.lastReceivedAddress;

        if (clientAddress == null) {
            logger.warning("Получена команда без адреса отправителя");
            return;
        }

        logger.info(
                "Обработка команды "
                        + command.getClass().getSimpleName()
                        + " от клиента "
                        + clientAddress);

        try {
            ExecutionResponse result = processCommand(command);

            if (result != null) {
                responseSender.submit(
                        () -> {
                            sendToAddress(result, clientAddress);
                        });
            }

        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Ошибка выполнения команды: " + command.getClass().getSimpleName(),
                    e);
            responseSender.submit(
                    () -> {
                        sendError("Ошибка выполнения команды: " + e.getMessage(), clientAddress);
                    });
        }
    }

    private ExecutionResponse processCommand(common.command.AbstractCommand command) {
        String username = command.getUsername();
        String password = command.getPassword();

        if (command instanceof Register) {
            return commandManager.getCommands().get("register").execute(command);
        }

        if (username == null || password == null) {
            return new ExecutionResponse(false, "Требуется авторизация");
        }

        if (!new server.dbmanagers.AuthManager().login(username, password)) {
            return new ExecutionResponse(
                    false, "Неверные логин/пароль. Для авторизации используйте login или register");
        }

        // Выполнение команды
        String commandName = command.getName().split(" ")[0];
        AbstractCommand cmd = commandManager.getCommands().get(commandName);
        if (cmd != null) {
            return cmd.execute(command);
        } else {
            return new ExecutionResponse(false, "Неизвестная команда: " + commandName);
        }
    }

    /** Обработка статусов подключения */
    private void handleProgramStatus(ProgramStatus status) {
        InetSocketAddress clientAddress = receivingManager.lastReceivedAddress;

        if (clientAddress == null) {
            logger.warning("Статус программы получен без адреса отправителя");
            return;
        }

        logger.info("Обработка статуса: " + status + " от клиента: " + clientAddress);

        switch (status) {
            case CLIENT_CONNECTS:
                addAddress(clientAddress);
                sendToAddress(ProgramStatus.SERVER_CONNECTS, clientAddress);
                break;

            case CLIENT_DISCONNECTS:
                deleteAddress(clientAddress);
                break;

            default:
                logger.warning("Неизвестный статус: " + status);
        }
    }

    /** Отправка объекта по конкретному адресу */
    public void sendToAddress(Object object, InetSocketAddress address) {
        responseSender.submit(
                () -> {
                    try {
                        sendingManager.send(port, address, object, null);
                        logger.fine("Отправлен объект клиенту " + address);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Ошибка отправки клиенту " + address, e);
                    }
                });
    }

    /** Отправка сообщения об ошибке */
    private void sendError(String errorMessage, InetSocketAddress address) {
        responseSender.submit(
                () -> {
                    try {
                        ExecutionResponse errorResponse =
                                new ExecutionResponse(false, errorMessage);
                        sendingManager.send(port, address, errorResponse, null);
                    } catch (Exception e) {
                        logger.log(
                                Level.SEVERE, "Не удалось отправить ошибку клиенту " + address, e);
                    }
                });
    }

    /** Отправка объекта всем подключенным клиентам */
    public void sendAll(Object object) {
        if (sessions.isEmpty()) {
            logger.fine("Нет подключенных клиентов для широковещательной отправки");
            return;
        }

        logger.info("Широковещательная отправка для " + sessions.size() + " клиентов");

        sessions.forEach(
                address -> {
                    responseSender.submit(
                            () -> {
                                try {
                                    sendingManager.send(port, address, object, null);
                                } catch (Exception e) {
                                    logger.log(
                                            Level.SEVERE, "Ошибка отправки клиенту " + address, e);
                                }
                            });
                });
    }

    /** Широкая вещательная отправка */
    public void broadcast(Object object) {
        responseSender.submit(
                () -> {
                    try (DatagramSocket broadcastSocket = new DatagramSocket()) {
                        broadcastSocket.setBroadcast(true);
                        sendingManager.send(
                                port,
                                new InetSocketAddress("255.255.255.255", port),
                                object,
                                broadcastSocket);
                        logger.info("Широкая вещательная отправка выполнена");
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Ошибка широковещательной отправки", e);
                    }
                });
    }

    public void addAddress(InetSocketAddress address) {
        if (sessions.add(address)) {
            logger.info("Добавлен клиент: " + address + " (всего: " + sessions.size() + ")");
        }
    }

    public void deleteAddress(InetSocketAddress address) {
        if (sessions.remove(address)) {
            logger.info("Удален клиент: " + address + " (осталось: " + sessions.size() + ")");
        }
    }

    public void somethingWithClient(ProgramStatus programStatus) {
        handleProgramStatus(programStatus);
    }

    public void stop() {
        isRunning = false;

        requestReader.shutdown();
        requestProcessor.shutdown();
        responseSender.shutdown();

        try {
            if (!requestReader.awaitTermination(5, TimeUnit.SECONDS)) requestReader.shutdownNow();
            if (!requestProcessor.awaitTermination(5, TimeUnit.SECONDS))
                requestProcessor.shutdownNow();
            if (!responseSender.awaitTermination(5, TimeUnit.SECONDS)) responseSender.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (receivingManager != null) receivingManager.close();
        if (sendingManager != null) sendingManager.stop();

        sessions.clear();
        logger.info("UDPManager остановлен");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    public HashSet<InetSocketAddress> getSessions() {
        return new HashSet<>(sessions);
    }

    public ReceivingManager getReceivingManager() {
        return receivingManager;
    }

    public SendingManager getSendingManager() {
        return sendingManager;
    }
}
