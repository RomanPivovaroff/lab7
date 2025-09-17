package server.network;

import common.utility.ProgramStatus;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.command.ExecutionResponse;

public class UDPManager {
    private int port = 46525;
    private final ReceivingManager receivingManager;
    private final SendingManager sendingManager;
    private HashSet<InetSocketAddress> sessions = new HashSet<>();
    private static final Logger logger = Logger.getLogger(UDPManager.class.getName());

    private volatile boolean isRunning = false;

    public UDPManager(int port, SendingManager sendingManager, ReceivingManager receivingManager)
            throws IOException {
        this.port = port;
        this.sendingManager = sendingManager;
        this.receivingManager = receivingManager;

        // Инициализируем неблокирующий приемник
        this.receivingManager.initialize(port);

        // Инициализируем отправку
        this.sendingManager.initialize();

        isRunning = true;
        logger.info("UDPManager запущен в неблокирующем режиме на порту " + port);
    }

    /**
     * Основной метод для проверки и обработки входящих данных Должен вызываться в основном цикле
     * программы
     */
    public void processIncoming() {
        if (!isRunning) {
            logger.warning("UDPManager не запущен");
            return;
        }

        try {
            Object receivedObject = receivingManager.receive();

            if (receivedObject != null) {
                processReceivedObject(receivedObject);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка обработки входящих данных", e);
        }
    }

    /** Обработка полученного объекта */
    private void processReceivedObject(Object object) {
        if (object instanceof ProgramStatus) {
            ProgramStatus status = (ProgramStatus) object;
            somethingWithClient(status);
        } else if (object instanceof common.command.RemoteCommand) {
            common.command.RemoteCommand command = (common.command.RemoteCommand) object;
            handleRemoteCommand(command);
        } else {
            logger.warning(
                    "Получен неизвестный тип объекта: "
                            + (object != null ? object.getClass().getName() : "null"));
        }
    }

    /** Обработка RemoteCommand от клиента */
    private void handleRemoteCommand(common.command.RemoteCommand command) {
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
            // Здесь должна быть логика обработки команды
            // Например: commandManager.executeCommand(command)

            Object result = executeCommand(command);

            // Отправляем результат обратно клиенту
            if (result != null) {
                send(result);
                logger.info("Результат отправлен клиенту " + clientAddress);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка выполнения команды", e);
            sendError("Ошибка выполнения команды: " + e.getMessage(), clientAddress);
        }
    }

    /** Заглушка для выполнения команды (должна быть заменена на реальный CommandManager) */
    private Object executeCommand(common.command.RemoteCommand command) {
        // TODO: Интегрировать с CommandManager
        logger.info("Выполнение команды: " + command.getClass().getSimpleName());
        return new ExecutionResponse(true, "Команда выполнена успешно");
    }

    /** Отправка объекта конкретному клиенту */
    public void send(Object object) {
        if (receivingManager.lastReceivedAddress != null) {
            sendToAddress(object, receivingManager.lastReceivedAddress);
        } else {
            logger.warning("Неизвестно кому отправлять - lastReceivedAddress is null");
        }
    }

    /** Отправка объекта по конкретному адресу */
    public void sendToAddress(Object object, InetSocketAddress address) {
        if (!isRunning) {
            logger.warning("Попытка отправки при остановленном UDPManager");
            return;
        }

        if (address == null) {
            logger.warning("Попытка отправки на null адрес");
            return;
        }

        try {
            sendingManager.send(port, address, object, null);
            logger.fine("Отправлен объект клиенту " + address);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка отправки клиенту " + address, e);
        }
    }

    /** Отправка сообщения об ошибке */
    private void sendError(String errorMessage, InetSocketAddress address) {
        try {
            ExecutionResponse errorResponse = new ExecutionResponse(false, errorMessage);
            sendToAddress(errorResponse, address);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Не удалось отправить сообщение об ошибке", e);
        }
    }

    /** Отправка объекта всем подключенным клиентам */
    public void sendAll(Object object) {
        if (sessions.isEmpty()) {
            logger.fine("Нет подключенных клиентов для широковещательной отправки");
            return;
        }

        logger.info(
                "Широковещательная отправка "
                        + object.getClass().getSimpleName()
                        + " для "
                        + sessions.size()
                        + " клиентов");

        for (InetSocketAddress address : sessions) {
            sendToAddress(object, address);
        }
    }

    /** Широкая вещательная отправка на все адреса в сети */
    public void broadcast(Object object) {
        try {
            // Для широковещательной отправки нужен DatagramSocket
            try (DatagramSocket broadcastSocket = new DatagramSocket()) {
                broadcastSocket.setBroadcast(true);
                sendingManager.send(
                        port,
                        new InetSocketAddress("255.255.255.255", port),
                        object,
                        broadcastSocket);
                logger.info("Широкая вещательная отправка выполнена");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка широковещательной отправки", e);
        }
    }

    /** Обработка подключения/отключения клиентов */
    public void somethingWithClient(ProgramStatus programStatus) {
        InetSocketAddress clientAddress = receivingManager.lastReceivedAddress;

        if (clientAddress == null) {
            logger.warning("Статус программы получен без адреса отправителя");
            return;
        }

        if (programStatus == ProgramStatus.CLIENT_CONNECTS) {
            addAddress(clientAddress);
            sendToAddress(ProgramStatus.SERVER_CONNECTS, clientAddress);
        } else if (programStatus == ProgramStatus.CLIENT_DISCONNECTS) {
            deleteAddress(clientAddress);
        }
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

    /** Проверка активности клиентов (должна вызываться периодически) */
    public void checkClientActivity() {
        logger.fine("Проверка активности " + sessions.size() + " клиентов");

        // Здесь можно реализовать логику проверки "живых" клиентов
        // Например, отправлять ping и удалять неответивших
    }

    /** Остановка менеджера */
    public void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;

        if (!sessions.isEmpty()) {
            logger.info("Оповещение " + sessions.size() + " клиентов об отключении сервера");
            sendAll(ProgramStatus.SERVER_DISCONNECTS);
        }

        if (receivingManager != null) {
            receivingManager.close();
        }

        if (sendingManager != null) {
            sendingManager.stop();
        }

        sessions.clear();
        logger.info("UDPManager остановлен");
    }

    /** Проверка работы менеджера */
    public boolean isRunning() {
        return isRunning;
    }

    /** Получение статистики */
    public String getStats() {
        return "UDPManager [порт: "
                + port
                + ", клиентов: "
                + sessions.size()
                + ", статус: "
                + (isRunning ? "работает" : "остановлен")
                + "]";
    }

    public int getPort() {
        return port;
    }

    public HashSet<InetSocketAddress> getSessions() {
        return new HashSet<>(sessions);
    }

    public void setSessions(HashSet<InetSocketAddress> sessions) {
        this.sessions = new HashSet<>(sessions);
        logger.info("Установлены сессии: " + sessions.size() + " клиентов");
    }

    public void setPort(int port) {
        this.port = port;
        logger.info("Установлен порт: " + port);
    }

    public ReceivingManager getReceivingManager() {
        return receivingManager;
    }

    public SendingManager getSendingManager() {
        return sendingManager;
    }

    /** Деструктор для безопасного закрытия */
    @Override
    protected void finalize() throws Throwable {
        try {
            stop();
        } finally {
            super.finalize();
        }
    }
}
