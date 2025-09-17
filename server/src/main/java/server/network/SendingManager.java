package server.network;

import common.command.ExecutionResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendingManager {
    private int maxWorkerCount = 50;
    private static final Logger logger = Logger.getLogger(SendingManager.class.getName());

    private DatagramChannel channel;
    private boolean isInitialized = false;

    public SendingManager() {
        // Конструктор без инициализации
    }

    /** Инициализация отправки */
    public void initialize() throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.isInitialized = true;
        logger.info("SendingManager инициализирован");
    }

    /** Отправка объекта */
    public void send(int port, InetSocketAddress address, Object object, DatagramSocket socket)
            throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("SendingManager не инициализирован");
        }

        byte[] data = serializeToXml(object);

        if (socket != null) {
            sendWithSocket(socket, address, data);
        } else {
            sendWithChannel(address, data);
        }
    }

    private void sendWithChannel(InetSocketAddress address, byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int bytesSent = channel.send(buffer, address);

        if (bytesSent > 0) {
            logger.fine("Отправлено " + bytesSent + " байт клиенту " + address);
        } else {
            logger.warning("Не удалось отправить данные клиенту " + address);
        }
    }

    private void sendWithSocket(DatagramSocket socket, InetSocketAddress address, byte[] data)
            throws IOException {
        java.net.DatagramPacket packet = new java.net.DatagramPacket(data, data.length, address);
        socket.send(packet);
        logger.fine("Отправлено " + data.length + " байт через socket клиенту " + address);
    }

    /** Сериализация объекта в XML */
    private byte[] serializeToXml(Object object) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        marshaller.marshal(object, outputStream);

        byte[] xmlData = outputStream.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(4 + xmlData.length);
        buffer.putInt(xmlData.length);
        buffer.put(xmlData);

        return buffer.array();
    }

    public void stop() {
        isInitialized = false;
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка закрытия канала отправки", e);
        }
        logger.info("SendingManager остановлен");
    }

    /** Вспомогательный метод для отправки ошибок */
    public void sendError(InetSocketAddress address, String errorMessage) throws Exception {
        send(0, address, new ExecutionResponse(false, errorMessage), null);
    }

    /** Вспомогательный метод для отправки успешных ответов */
    public void sendSuccess(InetSocketAddress address, String successMessage) throws Exception {
        send(0, address, new ExecutionResponse(true, successMessage), null);
    }
}
