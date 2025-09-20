package common.command;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@XmlRootElement
public class Login extends AbstractCommand implements LoginCommand {
    public Login(String username, String password) {
        super("Login", "авторизация пользователя");
        setUsername(username);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
        }
        md.update(password.getBytes());
        byte[] digest = md.digest();
        BigInteger no = new BigInteger(1, digest);

        String hashtext = no.toString(16);

        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        setPassword(hashtext);
    }

    public Login() {
        super("login", "авторизация пользователя");
    }
}
