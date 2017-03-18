package exception;

/**
 * Created by luciano on 18/03/17.
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
