package common.command;

import common.entity.Worker;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Класс команды для добавления элемента в коллекцию, если его значение превышает значение
 * наибольшего элемента этой коллекции.
 */
@XmlRootElement
public class AddIfMax extends AbstractCommand {
    private Worker worker;

    public AddIfMax(Worker worker) {
        super(
                "add_if_max {element}",
                "добавить новый элемент в коллекцию если его"
                        + " значение превышает значение наибольшего элемента этой коллекции.");
        this.worker = worker;
    }

    public AddIfMax() {
        super(
                "add_if_max {element}",
                "добавить новый элемент в коллекцию если его"
                        + " значение превышает значение наибольшего элемента этой коллекции.");
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @XmlElement
    public Worker getWorker() {
        return worker;
    }
}
