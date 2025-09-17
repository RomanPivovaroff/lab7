package server.utility;

import common.command.AbstractCommand;
import common.command.ExecutionResponse;
import common.command.Register;
import common.utility.Console;
import common.utility.ProgramStatus;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.network.UDPManager;

public class ServerCommandProcessor {
    private static final Logger logger = Logger.getLogger(ServerCommandProcessor.class.getName());
    private final UDPManager udpManager;
    private final Console console;
    private final CollectionManager collectionManager;
    private final CommandManager commandManager;
    private volatile boolean isRunning = false;

    public ServerCommandProcessor(
            Console console,
            CollectionManager collectionManager,
            UDPManager udpManager,
            CommandManager commandManager)
            throws IOException {
        this.udpManager = udpManager;
        this.console = console;
        this.collectionManager = collectionManager;
        this.commandManager = commandManager;
    }

    /** Запуск обработки команд в основном потоке */
    public void start() {
        isRunning = true;
        logger.info("Сервер начал обработку команд от клиентов");

        processCommands();
    }

    /** Основной цикл обработки входящих команд (в основном потоке) */
    private void processCommands() {
        while (isRunning) {
            try {
                Object receivedObject = udpManager.getReceivingManager().receive();

                if (receivedObject != null) {
                    processReceivedObject(receivedObject);
                }

                Thread.sleep(10);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Обработка команд прервана");
                break;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка в основном цикле обработки команд", e);
            }
        }
    }

    /** Обработка полученного объекта от клиента */
    private void processReceivedObject(Object receivedObject) {
        if (receivedObject instanceof ProgramStatus) {
            handleProgramStatus((ProgramStatus) receivedObject);
        } else if (receivedObject instanceof AbstractCommand) {
            handleRemoteCommand((AbstractCommand) receivedObject);
        } else {
            logger.warning(
                    "Получен неизвестный тип объекта: "
                            + (receivedObject != null
                                    ? receivedObject.getClass().getName()
                                    : "null"));
        }
    }

    /** Обработка команд от клиента */
    private void handleRemoteCommand(AbstractCommand command) {
        try {
            logger.info(
                    "Обработка команды: "
                            + command.getClass().getSimpleName()
                            + " от клиента: "
                            + udpManager.getReceivingManager().lastReceivedAddress);

            ExecutionResponse result =
                    new ExecutionResponse(
                            false,
                            "пользователь не авторизован или введен неверный пароль, для"
                                    + " авторизации используйте login или register");
            if (new server.dbmanagers.AuthManager()
                            .login(command.getUsername(), command.getPassword())
                    || command instanceof Register) {
                result =
                        commandManager
                                .getCommands()
                                .get(command.getName().split(" ")[0])
                                .execute(command);
            }

            // Отправляем результат обратно клиенту
            if (result != null) {
                udpManager.send(result);
                logger.info("Результат команды отправлен клиенту");
            }

        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Ошибка выполнения команды: " + command.getClass().getSimpleName(),
                    e);
            sendErrorResponse("Ошибка выполнения команды: " + e.getMessage());
        }
    }

    /** Обработка статусов подключения */
    private void handleProgramStatus(ProgramStatus status) {
        logger.info(
                "Обработка статуса: "
                        + status
                        + " от клиента: "
                        + udpManager.getReceivingManager().lastReceivedAddress);
        udpManager.somethingWithClient(status);
    }

    /** Отправка сообщения об ошибке */
    private void sendErrorResponse(String errorMessage) {
        try {
            udpManager.send(new ExecutionResponse(true, errorMessage));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Не удалось отправить ошибку клиенту", ex);
        }
    }

    /** Остановка сервера */
    public void stop() {
        isRunning = false;
        if (udpManager != null) {
            udpManager.stop();
        }
        logger.info("Сервер остановлен");
    }

    public UDPManager getUdpManager() {
        return udpManager;
    }
}
