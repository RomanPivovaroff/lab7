package common.command;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/** Абстрактный класс от которого наследуются все команды, используемые пользователем. */
@XmlRootElement
public abstract class AbstractCommand implements Serializable, RemoteCommand {
    private final String description;
    private final String name;
    private String username = "";
    private String password = "";

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

    public AbstractCommand() {
        this.name = null;
        this.description = null;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @XmlElement
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
