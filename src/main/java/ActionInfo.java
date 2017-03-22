import java.util.LongSummaryStatistics;

/**
 * Created by ms0371 on 3/22/17.
 */
public class ActionInfo {

    private String url;
    private Long elapsedTime;
    private Long downloadedBytes;

    public ActionInfo(String url,long elapsedTime,long downloadedBytes) {
        this.url = url;
        this.elapsedTime = elapsedTime;
        this.downloadedBytes = downloadedBytes;
    }


    public String getUrl() {
        return this.url;
    }

    public Long getElapsedTime() {
        return  this.elapsedTime;
    }

    public Long getDownloadedBytes() {
        return this.downloadedBytes;
    }

}
