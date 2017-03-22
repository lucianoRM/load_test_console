import okhttp3.Response;

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

    private BlockingQueue<ActionInfo> incomingActionInfoQueue;
    private int timeSlice = Configuration.getReportingTimeSlice();
    private Map<String,Map<String,Long>> temporalValues;


    public Reporter(BlockingQueue<ActionInfo> incomingActionInfoQueue) {
        this.incomingActionInfoQueue = incomingActionInfoQueue;
        temporalValues = new HashMap<>();
    }

    private void updateTemporalValues(ActionInfo actionInfo) {
        long previousElapsedTime = 0;
        long previousDownloadedBytes = 0;
        long previousUserCount = 0;
        if(this.temporalValues.containsKey(actionInfo.getUrl())){
            previousElapsedTime = this.temporalValues.get(actionInfo.getUrl()).get(ELAPSED_TIME_KEY);
            previousDownloadedBytes = this.temporalValues.get(actionInfo.getUrl()).get(DOWNLOADED_BYTES_KEY);
            previousUserCount = this.temporalValues.get(actionInfo.getUrl()).get(USER_COUNT_KEY);
        }else {
            Map<String,Long> newMap = new HashMap<>();
            this.temporalValues.put(actionInfo.getUrl(),newMap);
        }
        this.temporalValues.get(actionInfo.getUrl()).put(ELAPSED_TIME_KEY,previousElapsedTime + actionInfo.getElapsedTime());
        this.temporalValues.get(actionInfo.getUrl()).put(DOWNLOADED_BYTES_KEY,previousDownloadedBytes + actionInfo.getDownloadedBytes());
        this.temporalValues.get(actionInfo.getUrl()).put(USER_COUNT_KEY,previousUserCount + 1);


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
                e.printStackTrace();
            }
            long tEnd = System.currentTimeMillis();
            elapsedTime += (tEnd - tStart);
        }
    }

    public void run() {

        while(true) {
            this.reportTimeSlice();
            System.out.println(this.temporalValues);
            this.temporalValues = new HashMap<>();
        }

    }
}
