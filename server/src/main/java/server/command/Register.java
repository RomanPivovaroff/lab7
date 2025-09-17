package server.command;

import common.command.ExecutionResponse;
import server.dbmanagers.AuthManager;

public class Register extends AbstractCommand {
    public Register() {
        super("register", "регестрация пользователя");
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand command) {
        AuthManager ah = new AuthManager();
        common.command.Register reg = (common.command.Register) command;
        if (ah.register(reg.getUsername(), reg.getPassword()))
            return new ExecutionResponse(true, "Вы успешно зарегитрировались.");
        else
            return new ExecutionResponse(
                    true, "Пользователь с таким именем уже зарегестрирован в системе.");
    }
}
