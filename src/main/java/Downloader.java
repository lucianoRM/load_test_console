import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by ms0371 on 3/22/17.
 */
public class Downloader implements Runnable {

    private String url;
    private String resourceType;
    private OkHttpClient client;
    private BlockingQueue<DownloaderInfo> outgoingInfoQueue;
    private BlockingQueue<MonitorInfo> outgoingMonitorQueue;
    private Logger logger = LogManager.getLogger(this.getClass());


    public Downloader(String url,String resourceType,BlockingQueue<DownloaderInfo> outgoingInfoQueue,BlockingQueue<MonitorInfo> outgoingMonitorQueue) {
        this.url = url;
        this.resourceType = resourceType;
        this.outgoingInfoQueue = outgoingInfoQueue;
        this.outgoingMonitorQueue = outgoingMonitorQueue;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES);

        this.client = builder.build();
    }

    private void notifyErrorToUser() {
        DownloaderInfo downloaderInfo = new DownloaderInfo(this.url,(long)0,(long)0,true);
        this.outgoingInfoQueue.add(downloaderInfo);
    }

    private Request createRequest() {
        try {
            return new Request.Builder()
                    .url(this.url)
                    .build();
        }catch(IllegalArgumentException e) {
            this.logger.error("Closing downloader" + e);
            this.notifyErrorToUser();
            return null;
        }
    }

    private void notifyMonitor(boolean isStarting) {
        MonitorInfo monitorInfo = new MonitorInfo();
        if(isStarting) {
            monitorInfo.notifyActionStarted(this.resourceType);
        }else {
            monitorInfo.notifyActionEnded(this.resourceType);
        }
        this.outgoingMonitorQueue.add(monitorInfo);
    }


    public void run() {

        this.logger.info("Started");
        long tStart = System.currentTimeMillis();
        this.notifyMonitor(true);
        Request request = this.createRequest();
        if(request == null) return;
        Response response = null;
        try {
            this.logger.info("Requesting resource");
            response = this.client.newCall(request).execute();
            this.logger.info("Got resource");
        }catch(IOException e) {
            this.logger.error("Closing downloader " + e);
            this.notifyErrorToUser();
            return;
        }
        long tEnd = System.currentTimeMillis();
        long time;
        if(response.code() < 400) {
            time = tEnd - tStart;
        }else {
            time = -1; //This is the code to notify an error in thw request
        }
        DownloaderInfo downloaderInfo = new DownloaderInfo(this.url,time,response.body().contentLength(),false);
        this.outgoingInfoQueue.add(downloaderInfo);
        this.notifyMonitor(false);
        this.logger.info("Finished");
    }


}
