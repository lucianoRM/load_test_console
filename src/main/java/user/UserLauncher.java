package user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reports.MonitorInfo;
import user.User;
import utils.Configuration;
import utils.SessionControl;

import java.util.List;
import java.util.concurrent.*;

/**
 * Created by luciano on 19/03/17.
 *
 * Reads a queue and launches a user for every number read in the queue.
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

    private void readQueueAndLaunchUsers() throws InterruptedException,OutOfMemoryError{

        Integer newUsers = this.incomingUsersQueue.poll(Configuration.getTimeout(), TimeUnit.MILLISECONDS);
        logger.info("Read queue");
        if(newUsers == null) {
            logger.info("Queue read timed out");
            return;
        }
        for(int i = 0; i < newUsers; i++) {
            try {
                this.usersPool.execute(new User(this.scriptActions, this.userReportingQueue, this.userMonitorInfoQueue));
                logger.info("Launched User");
            }catch(OutOfMemoryError e) {
                this.logger.error("Could not create new user, exiting " + e);
                throw new OutOfMemoryError();
            }
        }

    }

    public void run() {

        logger.info("Started");
        while(SessionControl.shouldRun()) {
            try {
                readQueueAndLaunchUsers();
            } catch (InterruptedException e) {
                this.logger.warn("Interrupted while locked " + e);
                break;
            } catch (OutOfMemoryError e) {
                this.logger.error("Could not create new user, exiting " + e);
                break;
            } catch (RejectedExecutionException e){
                this.logger.error("Could not create new user, exiting " + e);
                break;
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
