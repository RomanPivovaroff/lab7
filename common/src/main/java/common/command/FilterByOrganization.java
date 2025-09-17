package common.command;

import common.entity.Organization;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FilterByOrganization extends AbstractCommand {
    private Organization organization;

    public FilterByOrganization(Organization organization) {
        super(
                "filter_by_organization {organization}",
                "вывести элементы, значение поля organization которых равно заданному");
        this.organization = organization;
    }

    public FilterByOrganization() {
        super(
                "filter_by_organization {organization}",
                "вывести элементы, значение поля organization которых равно заданному");
    }

    @XmlElement
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
