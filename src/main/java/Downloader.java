import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Created by ms0371 on 3/22/17.
 */
public class Downloader implements Runnable {

    private String url;
    private OkHttpClient client = new OkHttpClient();

    public Downloader(String url) {
        this.url = url;
    }

    private Request createRequest() {
        return new Request.Builder()
                .url(this.url)
                .build();
    }

    public void run() {

        Request request = this.createRequest();
        Response response;
        try {
            response = this.client.newCall(request).execute();
            System.out.println(response.body().string());
        }catch(IOException e) {
            e.printStackTrace();
        }
    }


}
