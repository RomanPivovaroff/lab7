package common.command;

import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'clear'. Очищает коллекцию. */
@XmlRootElement
public class Clear extends AbstractCommand {

    public Clear() {
        super("clear", "очистить коллекцию");
    }
}
