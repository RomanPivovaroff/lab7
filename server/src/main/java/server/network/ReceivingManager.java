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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceivingManager {
    public InetSocketAddress lastReceivedAddress = null;
    private static final Logger logger = Logger.getLogger(ReceivingManager.class.getName());

    private final ExecutorService readExecutor = Executors.newFixedThreadPool(2);

    private DatagramChannel channel;
    private Selector selector;
    private boolean isInitialized = false;

    public ReceivingManager() {}

    public void initialize(int port) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(new InetSocketAddress(port));

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        this.isInitialized = true;
        logger.info("ReceivingManager инициализирован с FixedThreadPool");
    }

    public Object receive() {
        if (!isInitialized) {
            logger.warning("ReceivingManager не инициализирован!");
            return null;
        }

        try {
            int readyChannels = selector.selectNow();
            if (readyChannels > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        // Обработка в fixed thread pool
                        return readExecutor.submit(this::processIncomingData).get();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка приема данных", e);
        }
        return null;
    }

    private Object processIncomingData() {
        ByteBuffer buffer = ByteBuffer.allocate(65535);
        buffer.clear();

        try {
            InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);
            if (clientAddress != null) {
                buffer.flip();
                lastReceivedAddress = clientAddress;
                return processReceivedData(buffer, clientAddress);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения данных", e);
        }
        return null;
    }

    private Object processReceivedData(ByteBuffer buffer, InetSocketAddress clientAddress) {
        try {
            if (buffer.remaining() < 4) return null;

            int xmlLength = buffer.getInt();
            if (xmlLength <= 0 || xmlLength > buffer.remaining()) return null;

            byte[] xmlData = new byte[xmlLength];
            buffer.get(xmlData);

            return deserializeFromXml(xmlData);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка обработки данных от " + clientAddress, e);
            return null;
        }
    }

    private Object deserializeFromXml(byte[] xmlData) throws Exception {
        String xmlString = new String(xmlData, StandardCharsets.UTF_8);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));

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
    }

    public void close() {
        isInitialized = false;
        readExecutor.shutdown();
        try {
            if (selector != null) selector.close();
            if (channel != null) channel.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка закрытия ресурсов", e);
        }
        logger.info("ReceivingManager закрыт");
    }
}
