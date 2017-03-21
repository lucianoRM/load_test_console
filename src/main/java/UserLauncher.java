import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by luciano on 19/03/17.
 */
public class UserLauncher implements Runnable{

    private BlockingQueue<Integer> incomingUsersQueue;
    private ExecutorService usersPool = Executors.newFixedThreadPool(Configuration.getConcurrentUsersCount());
    private List<Action> scriptActions;

    public UserLauncher(List<Action> scriptActions, BlockingQueue<Integer> incomingUsersQueue) {
        this.scriptActions = scriptActions;
        this.incomingUsersQueue = incomingUsersQueue;
    }

    private void readQueueAndLaunchUsers() throws InterruptedException{
        while(true) {
            int newUsers = this.incomingUsersQueue.take();
            for(int i = 0; i < newUsers; i++) {
                this.usersPool.execute(new User(this.scriptActions));
            }
        }
    }

    public void run() {
        try {
            readQueueAndLaunchUsers();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}