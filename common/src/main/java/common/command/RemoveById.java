package common.command;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'remove'. Удаляет элемент из коллекции по ид. */
@XmlRootElement
public class RemoveById extends AbstractCommand {
    private int id;

    public RemoveById(int id) {
        super("remove_by_id <ID>", "удалить элемент из коллекции по ID");
        this.id = id;
    }

    public RemoveById() {
        super("remove_by_id <ID>", "удалить элемент из коллекции по ID");
    }

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
