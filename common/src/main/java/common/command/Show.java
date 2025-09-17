package common.command;

import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'show'. Выводит все элементы коллекции. */
@XmlRootElement
public class Show extends AbstractCommand {

    public Show() {
        super(
                "show",
                "вывести в стандартный поток вывода все элементы коллекции в строковом"
                        + " представлении");
    }
}
