package common.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

/** Класс, описывающий рабочего. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Worker implements Comparable<Worker>, Validatable, Serializable {
    private Integer
            id; // оле не может быть null, Значение поля должно быть больше 0, Значение этого поля
    // должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; // Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; // Поле не может быть null
    private java.util.Date
            creationDate; // Поле не может быть null, Значение этого поля должно генерироваться
    // автоматически
    private Integer salary; // Поле не может быть null, Значение поля должно быть больше 0
    private ZonedDateTime startDate; // Поле не может быть null
    private Position position; // Поле не может быть null
    private Status status; // Поле может быть null
    private Organization organization; // Поле может быть null
    private String creator = "";

    /**
     * Конструктор
     *
     * @param name имя рабочего.(не пустая строка, не может быть null)
     * @param coordinates координатаы рабочего.(Coordinates, не может быть null)
     * @param salary зароботная плата рабочего(Integer > 0, не может быть null)
     * @param startDate дата вступления в рабочие обьязанности(ZonedDateTime, не может быть null)
     * @param position должность(Position, не может быть null)
     * @param status текущий статус(Status, может быть null)
     * @param organization организация в которой он работает(Organization, может быть null)
     */
    public Worker(
            Integer id,
            String name,
            Coordinates coordinates,
            Integer salary,
            java.time.ZonedDateTime startDate,
            Position position,
            Status status,
            Organization organization) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = new Date();
        this.salary = salary;
        this.startDate = startDate;
        this.position = position;
        this.status = status;
        this.organization = organization;
    }

    public Worker() {}

    /**
     * Метод для сравнения объектов Worker. Сравнение происходит по ид.
     *
     * @param o Объект для сравнения
     * @return id объекта - id о
     */
    public int compareTo(Worker o) {
        return Integer.compare(this.id, o.id);
    }

    @XmlElement
    public Integer getId() {
        return id;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    @XmlElement
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @XmlElement
    public java.util.Date getCreationDate() {
        return creationDate;
    }

    @XmlElement
    public Integer getSalary() {
        return salary;
    }

    @XmlElement
    @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
    public java.time.ZonedDateTime getStartDate() {
        return startDate;
    }

    @XmlElement
    public Position getPosition() {
        return position;
    }

    @XmlElement
    public Status getStatus() {
        return status;
    }

    @XmlElement
    public Organization getOrganization() {
        return organization;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setCreationDate(java.util.Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public void setStartDate(java.time.ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * Проверка валидности созданного объекта.
     *
     * @return true - если объект создан правильно, false в ином случае
     */
    @Override
    public boolean validate() {

        return id != null
                && id > 0
                && this.name != null
                && !this.name.trim().isEmpty()
                && coordinates != null
                && coordinates.validate()
                && (organization == null || organization.validate())
                && creationDate != null
                && salary != null
                && salary > 0
                && startDate != null;
    }

    public String validateWithDetails() {
        StringBuilder details = new StringBuilder();

        details.append("ID: ").append(id != null && id > 0 ? "✓" : "✗ (" + id + ")");
        details.append("\nName: ")
                .append(name != null && !name.trim().isEmpty() ? "✓" : "✗ (" + name + ")");
        details.append("\nCoordinates: ")
                .append(coordinates != null && coordinates.validate() ? "✓" : "✗");
        details.append("\nCreationDate: ").append(creationDate != null ? "✓" : "✗");
        details.append("\nSalary: ")
                .append(salary != null && salary > 0 ? "✓ (" + salary + ")" : "✗ (" + salary + ")");
        details.append("\nStartDate: ").append(startDate != null ? "✓" : "✗");
        details.append("\nPosition: ").append(position != null ? "✓" : "✗");
        details.append("\nOrganization: ")
                .append(organization == null ? "null ✓" : organization.validate() ? "✓" : "✗");

        if (coordinates != null && !coordinates.validate()) {
            details.append("\nCoordinates details: ").append(coordinates.validate());
        }
        if (organization != null && !organization.validate()) {
            details.append("\nOrganization details: ").append(organization.validateWithDetails());
        }

        return details.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker that = (Worker) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                coordinates,
                creationDate,
                salary,
                startDate,
                position,
                status,
                organization);
    }

    @Override
    public String toString() {
        return "Worker{\"id\": "
                + id
                + ", "
                + "\"name\": \""
                + name
                + "\", "
                + "\"creationDate\": \""
                + creationDate
                + "\", "
                + "\"coordinates\": \""
                + coordinates
                + "\", "
                + "\"salary\": \""
                + salary
                + "\", "
                + "\"startDate\": \""
                + startDate
                + "\", "
                + "\"position\": \""
                + (position == null ? "null" : position)
                + "\", "
                + "\"status\": \""
                + (status == null ? "null" : status)
                + "\", "
                + "\"organization\": \""
                + organization
                + "}";
    }

    @XmlElement
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
