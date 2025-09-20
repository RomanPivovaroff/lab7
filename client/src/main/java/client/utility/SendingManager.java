package client.utility;

import common.command.RemoteCommand;
import common.utility.Console;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendingManager {
    private final Console console;
    private final InetAddress serverAddress;

    // Fixed thread pool для отправки
    private final ExecutorService sendExecutor = Executors.newFixedThreadPool(2);

    public SendingManager(Console console, InetAddress serverAddress) {
        this.serverAddress = serverAddress;
        this.console = console;
    }

    public void send(
            RemoteCommand object, int serverPort, int clientPort, DatagramChannel channel) {
        sendExecutor.submit(
                () -> {
                    try {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        JAXBContext context = JAXBContext.newInstance(object.getClass());
                        Marshaller marshaller = context.createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
                        marshaller.marshal(object, outputStream);

                        byte[] xmlData = outputStream.toByteArray();
                        ByteBuffer buffer = ByteBuffer.allocate(4 + xmlData.length);
                        buffer.putInt(xmlData.length);
                        buffer.put(xmlData);
                        buffer.flip();

                        channel.send(buffer, new InetSocketAddress(serverAddress, serverPort));

                    } catch (IOException e) {
                        console.printError("Ошибка при отправке запроса: " + e.getMessage());
                    } catch (Exception e) {
                        console.printError("Ошибка сериализации команды: " + e.getMessage());
                    }
                });
    }

    public void shutdown() {
        sendExecutor.shutdown();
    }
}
