package reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import user.ActionInfo;
import utils.Configuration;
import utils.SessionControl;

import java.io.IOException;
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
    private Map<String, ReportPoint> reportTimes;
    private Map<String, ReportPoint> reportSizes;
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
            this.totalUsers+=actionInfo.getElapsedTime(); //Elapsed time will have a +1 or a -1 if a user is created or deleted
        }
        else {
            if(actionInfo.getElapsedTime() >= 0) {
                this.updateReportSizes(actionInfo);
                this.updateReportTimes(actionInfo);
            }
            this.updateReportErrors(actionInfo);

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
                this.logger.warn("Interrupted while locked in queue " + e);
            }
            long tEnd = System.currentTimeMillis();
            elapsedTime += (tEnd - tStart);
        }
    }

    private void clearScreen() {
//        System.out.print("\033[H\033[2J");
//        System.out.flush();
        try {
            Runtime.getRuntime().exec("clear");
        }catch(IOException e) {
            e.printStackTrace();
        }
    }


    private void displayReport() {
        this.clearScreen();
        System.out.println("Total users : " + this.totalUsers);
        for(Map.Entry<String,Map<String,Integer> > entry : this.reportErrors.entrySet()) {
            System.out.print(entry.getKey());

            //System.out.println("Requests");
            System.out.print(" Errors: " + entry.getValue().get(ERROR_KEY));
            System.out.print(" Success: " + entry.getValue().get(SUCCESS_KEY));

            if(this.reportTimes.containsKey(entry.getKey())) {
                //System.out.println("Times");
                System.out.print(" minTime: " + this.reportTimes.get(entry.getKey()).getMin() + "ms");
                System.out.print(" maxTime: " + this.reportTimes.get(entry.getKey()).getMax() + "ms");
                System.out.print(" avgTime: " + this.reportTimes.get(entry.getKey()).getAvg() + "ms");
            }
            System.out.println();

//            System.out.println("Sizes");
//            System.out.println("--MIN : " + this.reportSizes.get(entry.getKey()).getMin() + " ms");
//            System.out.println("--MAX : " + this.reportSizes.get(entry.getKey()).getMax() + " ms");
//            System.out.println("--AVG : " + this.reportSizes.get(entry.getKey()).getAvg() + " ms");


        }
    }

    public void run() {

        this.logger.info("Started");
        while(SessionControl.shouldRun()) {
            this.reportTimeSlice();
            this.displayReport();

            this.reportErrors = new HashMap<>();
            this.reportTimes = new HashMap<>();
            this.reportSizes = new HashMap<>();
        }
        this.logger.info("Finished");

    }
}
