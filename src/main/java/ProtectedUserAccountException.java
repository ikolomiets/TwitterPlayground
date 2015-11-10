import java.io.IOException;

public class ProtectedUserAccountException extends IOException {
    public ProtectedUserAccountException() {
    }

    public ProtectedUserAccountException(String message) {
        super(message);
    }

    public ProtectedUserAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtectedUserAccountException(Throwable cause) {
        super(cause);
    }
}
