package common.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

/** Класс, описывающий локацию(город для компании). */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Location implements Validatable, Serializable {
    private float x;
    private double y;
    private long z;
    private String name; // Строка не может быть пустой, Поле не может быть null

    /**
     * Конструктор
     *
     * @param x координата X(вещесвенное число)
     * @param y координата Y(вещесвенное число)
     * @param z координата Z(вещесвенное число)
     * @param name название локации (Строка, не может быть пустой, не может быть null)
     */
    public Location(float x, double y, long z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    public Location() {}

    @XmlElement
    public float getX() {
        return x;
    }

    @XmlElement
    public double getY() {
        return y;
    }

    @XmlElement
    public long getZ() {
        return z;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(long z) {
        this.z = z;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Проверка валидности созданного объекта.
     *
     * @return true - если объект создан правильно, false в ином случае
     */
    @Override
    public boolean validate() {
        return this.name != null && !this.name.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location that = (Location) o;

        if (!Objects.equals(x, that.x)) return false;
        if (!Objects.equals(y, that.y)) return false;
        if (!Objects.equals(z, that.z)) return false;

        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return "Location{\"x\": "
                + x
                + ", "
                + "\"y\": \""
                + y
                + "\", "
                + "\"z\": \""
                + z
                + "\", "
                + "\"name\": \""
                + name
                + "\" "
                + "} ";
    }
}
