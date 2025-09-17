package common.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/** Класс, задающий координаты рабочего. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Coordinates implements Validatable, Serializable {
    private int x; // Максимальное значение поля: 266
    private Float y; // Поле не может быть null

    /**
     * Конструктор
     *
     * @param x координата X(целочисленное число, максимальное значение: 266)
     * @param y координата Y(вещесвенное число, не может быть null)
     */
    public Coordinates(int x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Coordinates() {}

    @XmlElement
    public int getX() {
        return x;
    }

    @XmlElement
    public Float getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(Float y) {
        this.y = y;
    }

    /**
     * Проверка валидности созданного объекта.
     *
     * @return true - если объект создан правильно, false в ином случае
     */
    @Override
    public boolean validate() {
        return x <= 266 && y != null;
    }

    @Override
    public String toString() {
        return "Coordinates{\"x\": " + x + ", " + "\"y\": \"" + y + "\", " + "}";
    }
}
