import com.google.common.collect.ImmutableMap;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by luciano on 19/03/17.
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
    private OkHttpClient client;
    private BlockingQueue<DownloaderInfo> downloaderIncomingInfoQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<ActionInfo> reporterOutgoingInfoQueue;
    private BlockingQueue<MonitorInfo> monitorOutgoingInfoQueue;
    private int runningDownloaders = 0;
    private long startTime = 0;
    private Logger logger = LogManager.getLogger(this.getClass());


    public User(List<Action> scriptActions,BlockingQueue<ActionInfo> reporterOutgoingInfoQueue,BlockingQueue<MonitorInfo> monitorOutgoingInfoQueue) {
        this.reporterOutgoingInfoQueue = reporterOutgoingInfoQueue;
        this.monitorOutgoingInfoQueue = monitorOutgoingInfoQueue;
        this.scriptActions = scriptActions;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES);

        this.client = builder.build();
    }

    private void executeAction(Response response) {

        Document doc = null;
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.notifyActionStarted(URL_KEY);
        this.monitorOutgoingInfoQueue.add(monitorInfo);
        try {
            doc = Jsoup.parse(response.body().string(),"UTF-8", Parser.xmlParser());
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

    private void launchDownloaders(Elements elements) {

        for(Element element : elements) {
            String url = element.attr(SOURCE_ATTRIBUTE);
            if(url == "") {
                url = element.attr(HREF_ATTRIBUTE);
            }
            if(url != ""&& this.isLink(url)) { //"" means that the attribute does not exist in the tag
                this.downlaodersPool.execute(new Downloader(url,element.nodeName(),this.downloaderIncomingInfoQueue,this.monitorOutgoingInfoQueue));
                runningDownloaders++;
                this.logger.info("Started downloader");
            }
        }

    }


    private Request createRequest(Action action) {
        if(action.getMethod().equals(GET_ACTION_METHOD)) {
            return new Request.Builder()
                    .url(action.getUrl())
                    .build();
        }else {
            return new Request.Builder()
                    .url(action.getUrl())
                    .post(RequestBody.create(MediaType.parse("application/text; charset=utf-8"),action.getBody()))
                    .build();
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


    public void run() {

        logger.info("Started");

        /**
         * This is to notify the reporter that a new user is running
         */
        ActionInfo actionInfo = new ActionInfo(NEW_USER_URL,-1,-1);
        this.reporterOutgoingInfoQueue.add(actionInfo);
        while(SessionControl.shouldRun()) {
            for (Action action : this.scriptActions) {
                this.startTimer();
                Request request = this.createRequest(action);
                Response response = null;
                try {
                    logger.info("Requested url");
                    response = this.client.newCall(request).execute();
                    logger.info("Got url response");
                } catch (IOException e) {
                    this.logger.error("Exiting " + e);
                    break;
                }
                this.executeAction(response);
                this.reportInfo(action);
            }
        }
        try {
            this.downlaodersPool.shutdown();
            this.downlaodersPool.awaitTermination(Configuration.getTimeout(),TimeUnit.MILLISECONDS);
        }catch(InterruptedException e) {
            this.logger.error("Interrupted " + e);
        }
        logger.info("Finished");
    }
}
