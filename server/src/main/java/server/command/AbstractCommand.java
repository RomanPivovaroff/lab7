package server.command;

/** Абстрактный класс от которого наследуются все команды, используемые пользователем. */
public abstract class AbstractCommand implements Command {
    private final String description;
    private final String name;

    /**
     * Конструктор
     *
     * @param name имя команды
     * @param description описание работы команды и входных данных
     */
    public AbstractCommand(String name, String description) {
        this.description = description;
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
