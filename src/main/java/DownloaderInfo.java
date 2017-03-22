/**
 * Created by ms0371 on 3/22/17.
 */
public class DownloaderInfo {

    private String url;
    private Long downloadedBytes;
    private Long elapsedTime;

    public DownloaderInfo(String url,Long elapsedTime,Long downloadedBytes) {
        this.url = url;
        this.elapsedTime = elapsedTime;
        this.downloadedBytes = downloadedBytes;
    }

    public String getUrl() {
        return this.url;
    }

    public Long getElapsedTime() {
        return this.elapsedTime;
    }

    public Long getDownloadedBytes() {
        return this.downloadedBytes;
    }

}
