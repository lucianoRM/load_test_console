package exception;

/**
 * Created by luciano on 18/03/17.
 * Exception that indicates that the script is not as expected.
 */
public class InvalidScriptException extends Exception {

    public InvalidScriptException(String e) {
        super(e);
    }

    public InvalidScriptException() {
        super();
    }
}
