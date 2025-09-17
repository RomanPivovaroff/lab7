package common.command;

import common.entity.Worker;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'remove_greater'. удалить из коллекции все элементы, превышающие заданный. */
@XmlRootElement
public class RemoveGreater extends AbstractCommand {
    private Worker worker;

    public RemoveGreater(Worker worker) {
        super(
                "remove_greater {element}",
                "удалить из коллекции все элементы, превышающие заданный");
        this.worker = worker;
    }

    public RemoveGreater() {
        super(
                "remove_greater {element}",
                "удалить из коллекции все элементы, превышающие заданный");
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @XmlElement
    public Worker getWorker() {
        return worker;
    }
}
