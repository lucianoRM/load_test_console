import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by luciano on 19/03/17.
 */
public class UserLauncher implements Runnable{

    private BlockingQueue<Integer> incomingUsersQueue;
    private ExecutorService usersPool = Executors.newFixedThreadPool(Configuration.getConcurrentUsersCount());
    private BlockingQueue<ActionInfo> userReportingQueue;
    private BlockingQueue<MonitorInfo> userMonitorInfoQueue;
    private List<Action> scriptActions;
    private Logger logger = LogManager.getLogger(this.getClass());



    public UserLauncher(List<Action> scriptActions, BlockingQueue<Integer> incomingUsersQueue, BlockingQueue<ActionInfo> userReportingQueue, BlockingQueue<MonitorInfo> userMonitorInfoQueue) {
        this.scriptActions = scriptActions;
        this.incomingUsersQueue = incomingUsersQueue;
        this.userReportingQueue = userReportingQueue;
        this.userMonitorInfoQueue = userMonitorInfoQueue;

    }

    private void readQueueAndLaunchUsers() throws InterruptedException{

        Integer newUsers = this.incomingUsersQueue.poll(Configuration.getTimeout(), TimeUnit.MILLISECONDS);
        logger.info("Read queue");
        if(newUsers == null) {
            logger.info("Queue read timed out");
            return;
        }
        for(int i = 0; i < newUsers; i++) {
            this.usersPool.execute(new User(this.scriptActions,this.userReportingQueue,this.userMonitorInfoQueue));
            logger.info("Launched User");
        }

    }

    public void run() {

        logger.info("Started");
        while(SessionControl.shouldRun()) {
            try {
                readQueueAndLaunchUsers();
            } catch (InterruptedException e) {
                this.logger.warn("Interrupted while locked " + e);
            }
        }
        this.usersPool.shutdown();
        try {
            this.usersPool.awaitTermination(Configuration.getTimeout(), TimeUnit.MILLISECONDS);
        }catch(InterruptedException e){
            this.logger.error("Interrupted " + e);
        }
        logger.info("Finished");
    }
}
