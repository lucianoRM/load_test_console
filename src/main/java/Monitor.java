import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by luciano on 25/03/17.
 */
public class Monitor implements Runnable{

    private final String DOWNLOADING_THREADS_KEY = "downloading resources";
    private final String ANALYSING_THREADS_KEY = "analysing urls";

    private final ImmutableMap<String,Boolean> DOWNLOAD_TAGS = new ImmutableMap.Builder<String,Boolean>()
            .put("img",true)
            .put("script",true)
            .put("link",true)
            .build();


    private final ImmutableMap<String,Boolean> ANALYSIS_TAGS = new ImmutableMap.Builder<String,Boolean>()
            .put("url",true)
            .build();


    private BlockingQueue<MonitorInfo> incomingInfoQueue;
    private String filePath = Configuration.getMonitorFilePath();
    private Map<String,Integer> informationCount;

    public Monitor(BlockingQueue<MonitorInfo> incomingInfoQueue) {
        this.incomingInfoQueue = incomingInfoQueue;
        this.informationCount = new HashMap<>();
    }

    private void downloadStarted() {
        this.upsertInfo(this.DOWNLOADING_THREADS_KEY,1);
    }

    private void downloadEnded(String tag) {
        this.upsertInfo(this.DOWNLOADING_THREADS_KEY,-1);
        this.upsertInfo(tag,1);
    }

    private void analysisStarted() {
        this.upsertInfo(this.ANALYSING_THREADS_KEY,1);
    }

    private void analysisEnded(String tag) {
        this.upsertInfo(this.ANALYSING_THREADS_KEY,-1);
        this.upsertInfo(tag,1);
    }


    private void upsertInfo(String key,int value) {
        if(this.informationCount.containsKey(key)) {
            this.informationCount.put(key, this.informationCount.get(key) + value);
        }else {
            this.informationCount.put(key,value);
        }
    }


    private void update(MonitorInfo monitorInfo){
        if(this.DOWNLOAD_TAGS.containsKey(monitorInfo.getResourceKey())) {
            if(monitorInfo.isStarting()) {
                this.downloadStarted();
            }else{
                this.downloadEnded(monitorInfo.getResourceKey());
            }
        }else if(this.ANALYSIS_TAGS.containsKey(monitorInfo.getResourceKey())) {
            if(monitorInfo.isStarting()) {
                this.analysisStarted();
            }else{
                this.analysisEnded(monitorInfo.getResourceKey());
            }
        }
    }

    private void write() {
        System.out.println(this.informationCount);
    }

    public void run() {
        while(true) {
            try {
                MonitorInfo monitorInfo = this.incomingInfoQueue.poll(Configuration.getTimeout(), TimeUnit.MILLISECONDS);
                if(monitorInfo == null) continue;
                this.update(monitorInfo);
                this.write();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
