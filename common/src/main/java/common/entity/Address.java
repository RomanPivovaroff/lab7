package common.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

/** Класс, описывающий адрес организации. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Address implements Validatable, Serializable {
    private Location town; // Поле не может быть null
    private String street; // Длина строки не должна быть больше 174, Поле может быть null

    /**
     * Конструктор
     *
     * @param street улица.(строка, длиной не более 174 символов, не может быть null)
     * @param town город.(Location, не может быть null)
     */
    public Address(String street, Location town) {
        this.town = town;
        this.street = street;
    }

    public Address() {}

    @XmlElement
    public Location getTown() {
        return town;
    }

    @XmlElement
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setTown(Location town) {
        this.town = town;
    }

    /**
     * Проверка валидности созданного объекта.
     *
     * @return true - если объект создан правильно, false в ином случае
     */
    @Override
    public boolean validate() {
        return (street == null || street.length() <= 174) && town != null && town.validate();
    }

    public String validateWithDetails() {
        return "street: "
                + (street == null || street.length() <= 174
                        ? "✓"
                        : "✗ (length: " + (street != null ? street.length() : "null") + ")")
                + ", town: "
                + (town != null && town.validate() ? "✓" : "✗");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address that = (Address) o;

        if (!Objects.equals(street, that.street)) return false;

        return town.equals(that.town);
    }

    @Override
    public String toString() {
        return "Address{\"street\": " + street + ", " + "\"town\": \"" + town + "} ";
    }
}
