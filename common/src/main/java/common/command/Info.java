package common.command;

import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'info'. Выводит информацию о коллекции. */
@XmlRootElement
public class Info extends AbstractCommand {

    public Info() {
        super("info", "вывести информацию о коллекции");
    }
}
