package common.command;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PrintAscending extends AbstractCommand {

    public PrintAscending() {
        super("print_ascending", "вывести элементы коллекции в порядке возрастания");
    }
}
