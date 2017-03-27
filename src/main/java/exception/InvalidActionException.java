package exception;

/**
 * Created by luciano on 18/03/17.
 * Exception raised when an action is not valid. Actions must have a method and an url. If method is PUT or POST they
 * must have a body
 */
public class InvalidActionException extends Exception {
    public InvalidActionException() {
        super();
    }

    public InvalidActionException(Exception e){
        super(e);
    }

    public InvalidActionException(String e) {
        super(e);
    }
}
