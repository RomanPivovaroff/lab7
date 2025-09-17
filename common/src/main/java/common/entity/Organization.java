package common.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

/** Класс, описывающий организацию рабочего. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Organization implements Validatable, Serializable {
    private Float annualTurnover; // Поле не может быть null, Значение поля должно быть больше 0
    private OrganizationType type; // Поле может быть null
    private Address postalAddress; // Поле может быть null

    /**
     * Конструктор
     *
     * @param annualTurnover годовой оборот компании.(положительное число с плавабщей точкой, не
     *     может быть null)
     * @param type тип организации.(OrganizationType, может быть null)
     * @param postalAddress почтовый адрес организации (Address, может быть null)
     */
    public Organization(Float annualTurnover, OrganizationType type, Address postalAddress) {
        this.annualTurnover = annualTurnover;
        this.type = type;
        this.postalAddress = postalAddress;
    }

    public Organization() {}

    @XmlElement
    public Float getAnnualTurnover() {
        return annualTurnover;
    }

    @XmlElement
    public OrganizationType getType() {
        return type;
    }

    @XmlElement
    public Address getPostalAdress() {
        return postalAddress;
    }

    public void setAnnualTurnover(Float annualTurnover) {
        this.annualTurnover = annualTurnover;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public void setPostalAdress(Address postalAdress) {
        this.postalAddress = postalAdress;
    }

    /**
     * Проверка валидности созданного объекта.
     *
     * @return true - если объект создан правильно, false в ином случае
     */
    @Override
    public boolean validate() {
        return annualTurnover != null
                && annualTurnover > 0
                && (postalAddress == null || postalAddress.validate());
    }

    public String validateWithDetails() {
        StringBuilder details = new StringBuilder();
        details.append("annualTurnover: ")
                .append(
                        annualTurnover != null && annualTurnover > 0
                                ? "✓ (" + annualTurnover + ")"
                                : "✗ (" + annualTurnover + ")");
        details.append(", postalAddress: ")
                .append(postalAddress == null ? "null ✓" : postalAddress.validate() ? "✓" : "✗");

        if (postalAddress != null && !postalAddress.validate()) {
            details.append(" -> ").append(postalAddress.validateWithDetails());
        }

        return details.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Organization that = (Organization) o;

        if (!Objects.equals(annualTurnover, that.annualTurnover)) return false;

        if (type != that.type) return false;
        if (postalAddress == null && that.postalAddress != null) return false;
        if (postalAddress == null) return true;
        return postalAddress.equals(that.postalAddress);
    }

    @Override
    public String toString() {
        return "Organization{\"annualTurnover\": "
                + annualTurnover
                + ", "
                + "\"type\": \""
                + (type == null ? "null" : type)
                + "\", "
                + "\"postalAdress\": \""
                + postalAddress
                + "} ";
    }
}
