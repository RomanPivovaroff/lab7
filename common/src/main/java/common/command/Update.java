package common.command;

import common.entity.Worker;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'update'. Обновляет элемент коллекции. */
@XmlRootElement
public class Update extends AbstractCommand {
    private Worker worker;
    private int id;

    public Update(Worker worker, int id) {
        super("update <ID> {element}", "обновить значение элемента коллекции по ID");
        this.id = id;
        this.worker = worker;
    }

    public Update() {
        super("update <ID> {element}", "обновить значение элемента коллекции по ID");
    }

    @XmlElement
    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
