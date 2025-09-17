package server.command;

import common.command.ExecutionResponse;
import server.dbmanagers.AuthManager;

public class Login extends AbstractCommand {
    public Login() {
        super("", "");
    }

    @Override
    public ExecutionResponse execute(common.command.AbstractCommand commad) {
        AuthManager ah = new AuthManager();
        common.command.Login login = (common.command.Login) commad;
        if (ah.login(login.getUsername(), login.getPassword()))
            return new ExecutionResponse(true, "Вы успешно вошли в аккаунт.");
        else return new ExecutionResponse(true, "Вы указали неверное имя пользователя или пароль.");
    }
}
