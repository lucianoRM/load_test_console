import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Created by ms0371 on 3/22/17.
 */
public class Reporter implements Runnable{

    private static final String ELAPSED_TIME_KEY = "elapsed_time";
    private static final String DOWNLOADED_BYTES_KEY = "downloaded_bytes";
    private static final String USER_COUNT_KEY = "user_count";
    private static final String NEW_USER_URL = "new_user";
    private static final String ERROR_KEY = "error";
    private static final String SUCCESS_KEY = "success";


    private BlockingQueue<ActionInfo> incomingActionInfoQueue;
    private int timeSlice = Configuration.getReportingTimeSlice();
    private Map<String,ReportPoint> reportTimes;
    private Map<String,ReportPoint> reportSizes;
    private Map<String,Map<String,Integer>> reportErrors;
    private int totalUsers;
    private Logger logger = LogManager.getLogger(this.getClass());


    public Reporter(BlockingQueue<ActionInfo> incomingActionInfoQueue) {
        this.incomingActionInfoQueue = incomingActionInfoQueue;
        reportTimes = new HashMap<>();
        reportSizes = new HashMap<>();
        reportErrors = new HashMap<>();
        totalUsers = 0;

    }

    private void updateReportErrors(ActionInfo actionInfo) {
        Map<String,Integer> newErrors;
        if(!reportErrors.containsKey(actionInfo.getUrl())) {
            newErrors = new HashMap<>();
            newErrors.put(ERROR_KEY,0);
            newErrors.put(SUCCESS_KEY,0);
        }else {
            newErrors = this.reportErrors.get(actionInfo.getUrl());
        }
        if(actionInfo.getElapsedTime() == -1) { //Means that there was an error in the action request
            newErrors.put(ERROR_KEY,newErrors.get(ERROR_KEY) + 1);
        }else{
            newErrors.put(SUCCESS_KEY,newErrors.get(SUCCESS_KEY) + 1);
        }
        this.reportErrors.put(actionInfo.getUrl(),newErrors);
    }


    private void updateReportTimes(ActionInfo actionInfo) {
        ReportPoint reportPoint;
        if(this.reportTimes.containsKey(actionInfo.getUrl())) {
            reportPoint = this.reportTimes.get(actionInfo.getUrl());
        }else {
            reportPoint = new ReportPoint();
        }
        reportPoint.update(actionInfo.getElapsedTime());
        this.reportTimes.put(actionInfo.getUrl(),reportPoint);
    }

    private void updateReportSizes(ActionInfo actionInfo) {
        ReportPoint reportPoint;
        if(this.reportSizes.containsKey(actionInfo.getUrl())) {
            reportPoint = this.reportSizes.get(actionInfo.getUrl());
        }else {
            reportPoint = new ReportPoint();
        }
        reportPoint.update(actionInfo.getDownloadedBytes());
        this.reportSizes.put(actionInfo.getUrl(),reportPoint);
    }

    private void updateTemporalValues(ActionInfo actionInfo) {
        if(actionInfo.getUrl() == NEW_USER_URL) { //This means that a new user was created
            this.totalUsers++;
        }
        else {
            this.updateReportErrors(actionInfo);
            this.updateReportSizes(actionInfo);
            this.updateReportTimes(actionInfo);
        }


    }


    private void reportTimeSlice() {

        long elapsedTime = 0;
        while(elapsedTime < this.timeSlice) {
            long tStart =  System.currentTimeMillis();
            try {
                ActionInfo actionInfo = incomingActionInfoQueue.poll(this.timeSlice - elapsedTime, TimeUnit.MILLISECONDS);
                if(actionInfo != null) {
                    this.updateTemporalValues(actionInfo);
                }
            }catch (InterruptedException e){
                this.logger.warn("Interrupted while locked in queue");
            }
            long tEnd = System.currentTimeMillis();
            elapsedTime += (tEnd - tStart);
        }
    }

    private void displayReport() {
        System.out.println("Total users : " + this.totalUsers);
        for(Map.Entry<String,Map<String,Integer> > entry : this.reportErrors.entrySet()) {
            System.out.println(entry.getKey());

            System.out.println("Requests");
            System.out.println("--Errors : " + entry.getValue().get(ERROR_KEY));
            System.out.println("--Success : " + entry.getValue().get(SUCCESS_KEY));

            System.out.println("Times");
            System.out.println("--MIN : " + this.reportTimes.get(entry.getKey()).getMin() + " ms");
            System.out.println("--MAX : " + this.reportTimes.get(entry.getKey()).getMax() + " ms");
            System.out.println("--AVG : " + this.reportTimes.get(entry.getKey()).getAvg() + " ms");

            System.out.println("Sizes");
            System.out.println("--MIN : " + this.reportSizes.get(entry.getKey()).getMin() + " ms");
            System.out.println("--MAX : " + this.reportSizes.get(entry.getKey()).getMax() + " ms");
            System.out.println("--AVG : " + this.reportSizes.get(entry.getKey()).getAvg() + " ms");


        }
    }

    public void run() {

        this.logger.info("Started");
        while(SessionControl.shouldRun()) {
            this.reportTimeSlice();
            this.displayReport();

            this.totalUsers = 0;
            this.reportErrors = new HashMap<>();
            this.reportTimes = new HashMap<>();
            this.reportSizes = new HashMap<>();
        }
        this.logger.info("Finished");

    }
}
