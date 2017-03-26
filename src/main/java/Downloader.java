import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by ms0371 on 3/22/17.
 */
public class Downloader implements Runnable {

    private String url;
    private String resourceType;
    private OkHttpClient client = new OkHttpClient();
    private BlockingQueue<DownloaderInfo> outgoingInfoQueue;
    private BlockingQueue<MonitorInfo> outgoingMonitorQueue;
    private Logger logger = LogManager.getLogger(this.getClass());


    public Downloader(String url,String resourceType,BlockingQueue<DownloaderInfo> outgoingInfoQueue,BlockingQueue<MonitorInfo> outgoingMonitorQueue) {
        this.url = url;
        this.resourceType = resourceType;
        this.outgoingInfoQueue = outgoingInfoQueue;
        this.outgoingMonitorQueue = outgoingMonitorQueue;
    }

    private Request createRequest() {
        return new Request.Builder()
                .url(this.url)
                .build();
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
        Response response = null;
        try {
            this.logger.info("Requesting resource");
            response = this.client.newCall(request).execute();
            this.logger.info("Got resource");
        }catch(IOException e) {
            this.logger.error(e);
            this.logger.error("Exiting");
            return;
        }
        long tEnd = System.currentTimeMillis();
        DownloaderInfo downloaderInfo = new DownloaderInfo(this.url,tEnd - tStart,response.body().contentLength());
        this.outgoingInfoQueue.add(downloaderInfo);
        this.notifyMonitor(false);
        this.logger.info("Finished");
    }


}
