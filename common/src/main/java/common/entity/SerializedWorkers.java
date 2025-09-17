package common.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

/** Класс-оболочка для сериализации коллекции. */
@XmlRootElement(name = "serializedWorker")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SerializedWorkers implements Serializable {
    private ArrayList<Worker> workers;

    public SerializedWorkers() {
        this.workers = new ArrayList<>();
    }

    /**
     * Конструктор
     *
     * @param workers TreeSet содержащий объекты класса Worker
     */
    public SerializedWorkers(ArrayList<Worker> workers) {
        this.workers = workers;
    }

    @XmlElement(name = "worker")
    public ArrayList<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(ArrayList<Worker> workers) {
        this.workers = workers;
    }

    public TreeSet<Worker> toTreeSet() {
        return new TreeSet<>(workers);
    }
}
