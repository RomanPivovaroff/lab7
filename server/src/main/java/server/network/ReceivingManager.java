package server.network;

import common.command.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceivingManager {
    public InetSocketAddress lastReceivedAddress = null;
    private static final Logger logger = Logger.getLogger(ReceivingManager.class.getName());

    private DatagramChannel channel;
    private Selector selector;
    private boolean isInitialized = false;

    public ReceivingManager() {
        // Конструктор без инициализации - нужно вызвать initialize()
    }

    /** Инициализация неблокирующего канала */
    public void initialize(int port) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(new InetSocketAddress(port));

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        this.isInitialized = true;
        logger.info("Неблокирующий UDP приемник запущен на порту " + port);
    }

    /** Получение следующего объекта (неблокирующее) Возвращает null если нет данных */
    public Object receive() {
        if (!isInitialized) {
            logger.warning("ReceivingManager не инициализирован! Вызовите initialize() сначала.");
            return null;
        }

        try {
            // Неблокирующая проверка готовности каналов
            int readyChannels = selector.selectNow();

            if (readyChannels > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        Object receivedObject = processIncomingData();
                        keyIterator.remove();
                        return receivedObject;
                    }

                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при проверке селектора", e);
        }

        return null;
    }

    /** Обработка входящих данных */
    private Object processIncomingData() {
        ByteBuffer buffer = ByteBuffer.allocate(65535);
        buffer.clear();

        try {
            InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

            if (clientAddress != null) {
                buffer.flip();
                lastReceivedAddress = clientAddress;
                logger.info("Получены данные от клиента: " + clientAddress);
                return processReceivedData(buffer, clientAddress);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения данных из канала", e);
        }

        return null;
    }

    /** Обработка полученных данных из буфера */
    private Object processReceivedData(ByteBuffer buffer, InetSocketAddress clientAddress) {
        try {
            // Проверяем минимальный размер пакета (4 байта для длины)
            if (buffer.remaining() < 4) {
                logger.warning(
                        "Слишком короткий пакет от "
                                + clientAddress
                                + ": "
                                + buffer.remaining()
                                + " байт");
                return null;
            }

            // Читаем длину XML данных (первые 4 байта)
            int xmlLength = buffer.getInt();

            // Проверяем корректность длины
            if (xmlLength <= 0) {
                logger.warning("Некорректная длина данных от " + clientAddress + ": " + xmlLength);
                return null;
            }

            if (xmlLength > buffer.remaining()) {
                logger.warning(
                        "Заявленная длина превышает доступные данные от "
                                + clientAddress
                                + ": "
                                + xmlLength
                                + " > "
                                + buffer.remaining());
                return null;
            }

            if (xmlLength > 65000) {
                logger.warning(
                        "Слишком большой пакет от " + clientAddress + ": " + xmlLength + " байт");
                return null;
            }

            // Читаем XML данные
            byte[] xmlData = new byte[xmlLength];
            buffer.get(xmlData);

            // Десериализуем XML
            Object result = deserializeFromXml(xmlData);

            if (result != null) {
                logger.info(
                        "Успешно десериализован объект: "
                                + result.getClass().getSimpleName()
                                + " от "
                                + clientAddress);
            } else {
                logger.warning("Не удалось десериализовать данные от " + clientAddress);
            }

            return result;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка обработки данных от " + clientAddress, e);
            return null;
        }
    }

    /** Десериализация XML данных в объект */
    private Object deserializeFromXml(byte[] xmlData) throws Exception {
        String xmlString = new String(xmlData, StandardCharsets.UTF_8);

        // Логируем начало XML для отладки (первые 100 символов)
        if (logger.isLoggable(Level.FINE)) {
            String xmlPreview =
                    xmlString.length() > 100 ? xmlString.substring(0, 100) + "..." : xmlString;
            logger.fine("Десериализация XML: " + xmlPreview);
        }

        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));

        // Для RemoteCommand
        try {
            JAXBContext context =
                    JAXBContext.newInstance(
                            Show.class,
                            Info.class,
                            Add.class,
                            AddIfMax.class,
                            Clear.class,
                            FilterByOrganization.class,
                            PrintAscending.class,
                            PrintUniqueStatus.class,
                            RemoveById.class,
                            RemoveGreater.class,
                            Update.class,
                            Login.class,
                            Register.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            // Пробуем другие возможные типы, если RemoteCommand не подошел
            logger.warning("Не удалось десериализовать как RemoteCommand: " + e.getMessage());
            return null;
        }
    }

    /** Проверка инициализации */
    public boolean isInitialized() {
        return isInitialized;
    }

    /** Закрытие ресурсов */
    public void close() {
        isInitialized = false;

        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка закрытия селектора", e);
        }

        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка закрытия канала", e);
        }

        logger.info("ReceivingManager закрыт");
    }

    /** Получение информации о состоянии */
    public String getStatus() {
        if (!isInitialized) {
            return "Not initialized";
        }

        try {
            return "Channel open: "
                    + channel.isOpen()
                    + ", Selector open: "
                    + selector.isOpen()
                    + ", Last client: "
                    + lastReceivedAddress;
        } catch (Exception e) {
            return "Error getting status: " + e.getMessage();
        }
    }

    /** Деструктор для безопасного закрытия */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
