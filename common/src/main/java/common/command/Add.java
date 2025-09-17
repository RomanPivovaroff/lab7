package common.command;

import common.entity.Worker;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'add' для отправки серверу */
@XmlRootElement
public class Add extends AbstractCommand {
    private Worker worker;

    public Add(Worker worker) {
        super("add {element}", "добавить новый элемент в коллекцию");
        this.worker = worker;
    }

    public Add() {
        super("add {element}", "добавить новый элемент в коллекцию");
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @XmlElement
    public Worker getWorker() {
        return worker;
    }
}
