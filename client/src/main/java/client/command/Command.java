package client.command;

import common.command.ExecutionResponse;

/** Интерфейс для классов команд. */
public interface Command {
    /** Запускает цикл выполнения конкретной команды. */
    ExecutionResponse execute();
}
