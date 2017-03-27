package user.downloader;

/**
 * Created by ms0371 on 3/22/17.
 */
public class DownloaderInfo {

    private String url;
    private Long downloadedBytes;
    private Long elapsedTime;
    private boolean error;

    public DownloaderInfo(String url,Long elapsedTime,Long downloadedBytes,boolean error) {
        this.url = url;
        this.elapsedTime = elapsedTime;
        this.downloadedBytes = downloadedBytes;
        this.error = error;
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

    public boolean getError() {
        return this.error;
    }

}
