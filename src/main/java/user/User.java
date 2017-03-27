package user;

import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import reports.MonitorInfo;
import user.downloader.Downloader;
import user.downloader.DownloaderInfo;
import utils.Configuration;
import utils.SessionControl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by luciano on 19/03/17.
 * Handles the execution of the script.
 * Every user contains a pool of downloaders.
 * When the response is received, it is parsed and a downloader is raised for every resource found int the response.
 *
 */
public class User implements Runnable {

    private static final String GET_ACTION_METHOD = "GET";

    private static final String URL_KEY = "url";
    private static final String NEW_USER_URL = "new_user";

    private static final String SOURCE_ATTRIBUTE = "src";
    private static final String HREF_ATTRIBUTE = "href";

    private static final List<String> RESOURCES_TAGS = new ArrayList<String>() {{
        add("img");
        add("link");
        add("script");
    }};



    private List<Action> scriptActions;
    private ExecutorService downlaodersPool = Executors.newFixedThreadPool(Configuration.getConcurrentDownloadersCount());
    private HttpClient client;
    private BlockingQueue<DownloaderInfo> downloaderIncomingInfoQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<ActionInfo> reporterOutgoingInfoQueue;
    private BlockingQueue<MonitorInfo> monitorOutgoingInfoQueue;
    private int runningDownloaders = 0;
    private long startTime = 0;
    private Logger logger = LogManager.getLogger(this.getClass());


    public User(List<Action> scriptActions, BlockingQueue<ActionInfo> reporterOutgoingInfoQueue, BlockingQueue<MonitorInfo> monitorOutgoingInfoQueue) {
        this.reporterOutgoingInfoQueue = reporterOutgoingInfoQueue;
        this.monitorOutgoingInfoQueue = monitorOutgoingInfoQueue;
        this.scriptActions = scriptActions;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Configuration.getHttpTimeout())
                .setConnectTimeout(Configuration.getHttpTimeout())
                .setSocketTimeout(Configuration.getHttpTimeout())
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        this.client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    private void executeAction(HttpResponse response) throws OutOfMemoryError{

        Document doc = null;
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.notifyActionStarted(URL_KEY);
        this.monitorOutgoingInfoQueue.add(monitorInfo);
        try {
            doc = Jsoup.parse(EntityUtils.toString(response.getEntity()),"UTF-8", Parser.xmlParser());
        }catch(IOException e){
            this.logger.warn("JSoup parse exception " + e);
            return;
        }
        for(String tag : RESOURCES_TAGS) {
            Elements elements = doc.getElementsByTag(tag);
            launchDownloaders(elements);
        }
        monitorInfo = new MonitorInfo();
        monitorInfo.notifyActionEnded(URL_KEY);
        this.monitorOutgoingInfoQueue.add(monitorInfo);

    }

    private boolean isLink(String url) {
        return url.startsWith("http");
    }

    private void launchDownloaders(Elements elements) throws OutOfMemoryError{

        for(Element element : elements) {
            String url = element.attr(SOURCE_ATTRIBUTE);
            if(url == "") {
                url = element.attr(HREF_ATTRIBUTE);
            }
            if(url != "" && this.isLink(url)) { //"" means that the attribute does not exist in the tag
                try {
                    this.downlaodersPool.execute(new Downloader(url, element.nodeName(), this.downloaderIncomingInfoQueue, this.monitorOutgoingInfoQueue));
                    runningDownloaders++;
                    this.logger.info("Started downloader");
                }catch (OutOfMemoryError e){
                    this.logger.error("Could not create new downloader, exiting " + e);
                    throw new OutOfMemoryError();
                }catch (RejectedExecutionException e){
                    this.logger.error("Could not create new downloader, exiting " + e);
                    throw new OutOfMemoryError();
                }
            }
        }

    }


    private HttpUriRequest createRequest(Action action) {



        if(action.getMethod().equals(GET_ACTION_METHOD)) {
            return new HttpGet(action.getUrl());
        }else {
            HttpPost request = new HttpPost(action.getUrl());
            request.setHeader("Content-Type", "application/text");
            request.setEntity(new ByteArrayEntity(action.getBody().getBytes()));
            return  request;
        }


    }

    private void reportInfo(Action action) {

        long downloadedBytes = 0;

        while(this.runningDownloaders > 0){
            try {
                this.logger.info("Waiting for downloader info");
                DownloaderInfo downloaderInfo = this.downloaderIncomingInfoQueue.poll(Configuration.getTimeout(),TimeUnit.MILLISECONDS);
                if(downloaderInfo == null) {
                    this.logger.info("Info queue timed out");
                    continue;
                }
                this.logger.info("Read downloader info");
                if(!downloaderInfo.getError()) {
                    downloadedBytes+=downloaderInfo.getDownloadedBytes();
                }
                this.runningDownloaders--;

            }catch(InterruptedException e){
                this.logger.warn("Interrupted while locked in queue " + e);
            }
        }
        ActionInfo actionInfo = new ActionInfo(action.getUrl(),this.stopTimer(),downloadedBytes);
        this.reporterOutgoingInfoQueue.add(actionInfo);

    }

    private void startTimer() {
        this.startTime = System.currentTimeMillis();
    }

    private long stopTimer() {
        long currentTime = System.currentTimeMillis();
        long startTime = this.startTime;
        this.startTime = 0;
        return currentTime - startTime;
    }


    private void exit() {
        try{
            this.downlaodersPool.shutdown();
            this.downlaodersPool.awaitTermination(Configuration.getTimeout(),TimeUnit.MILLISECONDS);
            this.notifyUserStoped();
        }catch(InterruptedException e) {
            this.logger.error("Interrupted " + e);
        }
    }


    private void notifyUserStarted() {
        /**
         * This is to notify the reporter that a new user is running
         */
        ActionInfo actionInfo = new ActionInfo(NEW_USER_URL,1,-1);
        this.reporterOutgoingInfoQueue.add(actionInfo);
    }

    private void notifyUserStoped() {
        /**
         * This is to notify the reporter that a new user stopped running
         */
        ActionInfo actionInfo = new ActionInfo(NEW_USER_URL,-1,-1);
        this.reporterOutgoingInfoQueue.add(actionInfo);
    }


    private void notifyRequestError(Action action) {
        ActionInfo actionInfo = new ActionInfo(action.getUrl(),-1,-1); //-1 in elapsed time means that there was an error in the request
        this.reporterOutgoingInfoQueue.add(actionInfo);
    }

    public void run() {

        logger.info("Started");
        this.notifyUserStarted();
        while(SessionControl.shouldRun()) {
            for (Action action : this.scriptActions) {
                this.startTimer();
                HttpUriRequest request = this.createRequest(action);
                HttpResponse response = null;
                try {
                    logger.info("Requested url");
                    response = this.client.execute(request);
                    if(response.getStatusLine().getStatusCode() >= 400 ) {
                        logger.warn("Unsuccessful response in " + action.getUrl() + " : " + response.getStatusLine().getStatusCode());
                        EntityUtils.consumeQuietly(response.getEntity());
                        this.notifyRequestError(action);
                        continue;
                    }
                    logger.info("Got url response");
                } catch (IOException e) {
                    this.notifyRequestError(action);
                    this.logger.error("Connection error " + e);
                    continue;
                }
                try {
                    this.executeAction(response);
                }catch (OutOfMemoryError e){
                    this.exit();
                }
                this.reportInfo(action);
            }
        }
        this.exit();
        logger.info("Finished");
    }
}
