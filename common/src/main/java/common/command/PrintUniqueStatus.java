package common.command;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Команда 'Print_unique_status'. Вывести уникальные значения поля status всех элементов в
 * коллекции.
 */
@XmlRootElement
public class PrintUniqueStatus extends AbstractCommand {

    public PrintUniqueStatus() {
        super(
                "print_unique_status",
                "вывести уникальные значения поля status всех элементов в коллекции");
    }
}
