import usercreationpattern.UserCreationPattern;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by luciano on 19/03/17.
 */
public class UserGenerator implements Runnable{

    private UserCreationPattern userCreationPattern;
    private BlockingQueue<Integer> outgoingUsersQueue;

    public UserGenerator(UserCreationPattern userCreationPattern, BlockingQueue<Integer> usersQueue) {
        this.userCreationPattern = userCreationPattern;
        this.outgoingUsersQueue = usersQueue;
    }

    public void generateUsers() throws InterruptedException{
        int logicTime = 0;
        int lastUserValue = 0;
        while(true) {
            if(this.userCreationPattern.getPatternValues().containsKey(logicTime)) {
                lastUserValue = this.userCreationPattern.getPatternValues().get(logicTime);
            }
            this.outgoingUsersQueue.add(lastUserValue);
            logicTime++;
            TimeUnit.MILLISECONDS.sleep(this.userCreationPattern.getTimeSlice());
        }

    }

    public void run() {
        try {
            this.generateUsers();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
