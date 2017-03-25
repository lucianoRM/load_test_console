import com.google.common.collect.ImmutableMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    private static final String SOURCE_ATTRIBUTE = "src";
    private static final String URL_KEY = "url";

    private static final List<String> RESOURCES_TAGS = new ArrayList<String>() {{
        add("img");
        add("link");
        add("script");
    }};



    private List<Action> scriptActions;
    private ExecutorService downlaodersPool = Executors.newFixedThreadPool(Configuration.getConcurrentDownloadersCount());
    private OkHttpClient client = new OkHttpClient();
    private BlockingQueue<DownloaderInfo> downloaderIncomingInfoQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<ActionInfo> reporterOutgoingInfoQueue;
    private BlockingQueue<MonitorInfo> monitorOutgoingInfoQueue;
    private int runningDownloaders = 0;
    private long startTime = 0;

    public User(List<Action> scriptActions,BlockingQueue<ActionInfo> reporterOutgoingInfoQueue,BlockingQueue<MonitorInfo> monitorOutgoingInfoQueue) {
        this.reporterOutgoingInfoQueue = reporterOutgoingInfoQueue;
        this.monitorOutgoingInfoQueue = monitorOutgoingInfoQueue;
        this.scriptActions = scriptActions;
    }

    private void executeAction(Response response) {

        Document doc = null;
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.notifyActionStarted(URL_KEY);
        this.monitorOutgoingInfoQueue.add(monitorInfo);
        try {
            doc = Jsoup.parse(response.body().string(),"UTF-8", Parser.xmlParser());
        }catch(IOException e){
            e.printStackTrace();
        }
        for(String tag : RESOURCES_TAGS) {
            Elements elements = doc.getElementsByTag(tag);
            launchDownloaders(elements);
        }
        monitorInfo = new MonitorInfo();
        monitorInfo.notifyActionEnded(URL_KEY);
        this.monitorOutgoingInfoQueue.add(monitorInfo);

    }

    private void launchDownloaders(Elements elements) {

        for(Element element : elements) {
            String url = element.attr(SOURCE_ATTRIBUTE);
            if(url != "") { //"" means that the attribute does not exist in the tag
                this.downlaodersPool.execute(new Downloader(url,element.nodeName(),this.downloaderIncomingInfoQueue,this.monitorOutgoingInfoQueue));
                runningDownloaders++;
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
                    .post(null)
                    .build();
        }
    }

    private void reportInfo(Action action) {

        long downloadedBytes = 0;

        while(this.runningDownloaders > 0){
            try {
                DownloaderInfo downloaderInfo = this.downloaderIncomingInfoQueue.take();
                downloadedBytes+=downloaderInfo.getDownloadedBytes();
                this.runningDownloaders--;

            }catch(InterruptedException e){
                e.printStackTrace();
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

        for(Action action : this.scriptActions) {
            this.startTimer();
            Request request = this.createRequest(action);
            Response response = null;
            try {
                response = this.client.newCall(request).execute();
            }catch(IOException e) {
                e.printStackTrace();
            }
            this.executeAction(response);
            this.reportInfo(action);
        }
    }
}
