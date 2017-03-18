package exception;

/**
 * Created by luciano on 18/03/17.
 */
public class InvalidScriptException extends Exception {

    public InvalidScriptException(String e) {
        super(e);
    }

    public InvalidScriptException() {
        super();
    }
}
