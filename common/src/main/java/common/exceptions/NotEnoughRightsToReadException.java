package common.exceptions;

/** Класс - исключение, выбрасывается при недостаточных правах для чтения */
public class NotEnoughRightsToReadException extends Exception {

    public NotEnoughRightsToReadException() {}

    @Override
    public String getMessage() {
        return "Файл недоступен для чтения.";
    }
}
