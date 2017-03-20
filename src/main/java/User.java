import java.util.List;

/**
 * Created by luciano on 19/03/17.
 */
public class User implements Runnable {

    private List<Action> scriptActions;

    public User(List<Action> scriptActions) {
        this.scriptActions = scriptActions;
    }

    public void run() {
        System.out.println("HOLA!");
    }
}
