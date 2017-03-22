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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by luciano on 19/03/17.
 */
public class User implements Runnable {

    private static final String GET_ACTION_METHOD = "GET";
    private static final String SOURCE_ATTRIBUTE = "src";

    private static final List<String> RESOURCES_TAGS = new ArrayList<String>() {{
        add("img");
        add("link");
        add("img");
    }};



    private List<Action> scriptActions;
    private ExecutorService downlaodersPool = Executors.newFixedThreadPool(Configuration.getConcurrentDownloadersCount());
    private OkHttpClient client = new OkHttpClient();

    public User(List<Action> scriptActions) {
        this.scriptActions = scriptActions;
    }

    private void parseResponseAndStartDownloaders(Response response) {

        Document doc = null;
        try {
            doc = Jsoup.parse(response.body().string(),"UTF-8", Parser.xmlParser());
        }catch(IOException e){
            e.printStackTrace();
        }

        for(String tag : RESOURCES_TAGS) {
            Elements elements = doc.getElementsByTag(tag);
            launchDownloaders(elements);
        }


    }

    private void launchDownloaders(Elements elements) {

        for(Element element : elements) {
            String url = element.attr(SOURCE_ATTRIBUTE);
            if(url != "") System.out.println(url); //"" means that the attribute does not exist in the tag
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

    public void run() {

        for(Action action : this.scriptActions) {
            Request request = this.createRequest(action);
            Response response = null;
            try {
                response = this.client.newCall(request).execute();
            }catch(IOException e) {
                e.printStackTrace();
            }
            this.parseResponseAndStartDownloaders(response);
        }
    }
}
