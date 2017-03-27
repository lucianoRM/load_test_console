package user.downloader;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reports.MonitorInfo;
import utils.Configuration;


import java.io.IOException;
import java.util.concurrent.BlockingQueue;


/**
 * Created by ms0371 on 3/22/17.
 */
public class Downloader implements Runnable {

    private String url;
    private String resourceType;
    private CloseableHttpClient client;
    private BlockingQueue<DownloaderInfo> outgoingInfoQueue;
    private BlockingQueue<MonitorInfo> outgoingMonitorQueue;
    private Logger logger = LogManager.getLogger(this.getClass());


    public Downloader(String url, String resourceType, BlockingQueue<DownloaderInfo> outgoingInfoQueue, BlockingQueue<MonitorInfo> outgoingMonitorQueue) {
        this.url = url;
        this.resourceType = resourceType;
        this.outgoingInfoQueue = outgoingInfoQueue;
        this.outgoingMonitorQueue = outgoingMonitorQueue;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Configuration.getHttpTimeout())
                .setConnectTimeout(Configuration.getHttpTimeout())
                .setSocketTimeout(Configuration.getHttpTimeout())
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        this.client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

    }

    private void notifyErrorToUser() {
        DownloaderInfo downloaderInfo = new DownloaderInfo(this.url,(long)0,(long)0,true);
        this.outgoingInfoQueue.add(downloaderInfo);
    }

    private HttpUriRequest createRequest() {
        try {
            return new HttpGet(this.url);
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

    private long computeDownloadedBytes(HttpResponse response) {

        String str=null;
        try {
            str = EntityUtils.toString(response.getEntity());
            EntityUtils.consumeQuietly(response.getEntity());
        }catch(IOException e) {
            this.logger.warn("Error computing size " + e);
            return -1;
        }
        return str.length();
    }


    public void run() {

        this.logger.info("Started");
        long tStart = System.currentTimeMillis();
        this.notifyMonitor(true);
        HttpUriRequest request = this.createRequest();
        if(request == null) return;
        HttpResponse response = null;
        try {
            this.logger.info("Requesting resource");
            response = this.client.execute(request);
            this.logger.info("Got resource");
        }catch(IOException e) {
            this.logger.error("Closing downloader " + e);
            this.notifyErrorToUser();
            return;
        }
        long tEnd = System.currentTimeMillis();
        long time;
        if(response.getStatusLine().getStatusCode() >= 400 ) {
            time = tEnd - tStart;
        }else {
            time = -1; //This is the code to notify an error in the request
        }
        long size = this.computeDownloadedBytes(response);
        if (size < 0) {
            this.notifyErrorToUser();
            return;
        }
        DownloaderInfo downloaderInfo = new DownloaderInfo(this.url,time,size,false);
        this.outgoingInfoQueue.add(downloaderInfo);
        this.notifyMonitor(false);
        this.logger.info("Finished");
    }


}
