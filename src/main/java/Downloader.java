import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    public Downloader(String url,String resourceType,BlockingQueue<DownloaderInfo> outgoingInfoQueue) {
        this.url = url;
        this.resourceType = resourceType;
        this.outgoingInfoQueue = outgoingInfoQueue;
    }

    private Request createRequest() {
        return new Request.Builder()
                .url(this.url)
                .build();
    }

    public void run() {

        long tStart = System.currentTimeMillis();
        Request request = this.createRequest();
        Response response = null;
        try {
            response = this.client.newCall(request).execute();
        }catch(IOException e) {
            e.printStackTrace();
        }
        long tEnd = System.currentTimeMillis();
        DownloaderInfo downloaderInfo = new DownloaderInfo(this.url,tEnd - tStart,response.body().contentLength());
        this.outgoingInfoQueue.add(downloaderInfo);
    }


}
