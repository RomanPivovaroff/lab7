package common.command;

import jakarta.xml.bind.annotation.XmlRootElement;

/** Команда 'save'. Сохраняет коллекцию в файл. */
@XmlRootElement
public class Save extends AbstractCommand {

    public Save() {
        super("save", "сохранить коллекцию в файл");
    }
}
