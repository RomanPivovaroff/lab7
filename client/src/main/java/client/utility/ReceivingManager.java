package client.utility;

import common.command.ExecutionResponse;
import common.utility.Console;
import common.utility.StandardAppConsole;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceivingManager {
    private final Console console;

    // Cached thread pool для обработки ответов
    private final ExecutorService receiveExecutor = Executors.newCachedThreadPool();

    public ReceivingManager(StandardAppConsole console) {
        this.console = console;
    }

    public ExecutionResponse receive(DatagramChannel channel, int clientPort) {
        try {
            return receiveExecutor
                    .submit(
                            () -> {
                                try {
                                    ByteBuffer buffer = ByteBuffer.allocate(65536);
                                    buffer.clear();

                                    channel.receive(buffer);
                                    buffer.flip();

                                    if (buffer.remaining() < 4) {
                                        return new ExecutionResponse(
                                                false, "Получен слишком короткий пакет");
                                    }

                                    int dataLength = buffer.getInt();

                                    if (buffer.remaining() < dataLength) {
                                        return new ExecutionResponse(
                                                false, "Недостаточно данных в пакете");
                                    }

                                    byte[] xmlData = new byte[dataLength];
                                    buffer.get(xmlData);

                                    return deserializeFromXml(xmlData);

                                } catch (IOException e) {
                                    return new ExecutionResponse(
                                            false, "Сетевая ошибка: " + e.getMessage());
                                } catch (Exception e) {
                                    return new ExecutionResponse(
                                            false, "Ошибка обработки ответа: " + e.getMessage());
                                }
                            })
                    .get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private ExecutionResponse deserializeFromXml(byte[] xmlData) throws Exception {
        String xmlString = new String(xmlData, StandardCharsets.UTF_8);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));

        JAXBContext context = JAXBContext.newInstance(ExecutionResponse.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        return (ExecutionResponse) unmarshaller.unmarshal(inputStream);
    }

    public void shutdown() {
        receiveExecutor.shutdown();
    }
}
