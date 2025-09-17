package server.command;

/** Класс для обратной связи команд */
public class ExecutionResponse {
    private boolean isSucceeded;
    private String massage;

    public ExecutionResponse(boolean code, String s) {
        isSucceeded = code;
        massage = s;
    }

    public ExecutionResponse(String s) {
        this(true, s);
    }

    public boolean getIsSucceeded() {
        return isSucceeded;
    }

    public String getMassage() {
        return massage;
    }

    public String toString() {
        return String.valueOf(isSucceeded) + ";" + massage;
    }
}
