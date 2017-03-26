import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private final String URL_KEY = "url";
    private Logger logger = LogManager.getLogger(this.getClass());

    private final ImmutableMap<String,Boolean> DOWNLOAD_TAGS = new ImmutableMap.Builder<String,Boolean>()
            .put("img",true)
            .put("script",true)
            .put("link",true)
            .build();



    private BlockingQueue<MonitorInfo> incomingInfoQueue;
    private File monitorFile;
    private Map<String,Integer> informationCount;

    public Monitor(BlockingQueue<MonitorInfo> incomingInfoQueue) {
        this.incomingInfoQueue = incomingInfoQueue;
        this.informationCount = new HashMap<>();
        for(String key : this.DOWNLOAD_TAGS.keySet()) {
            this.informationCount.put(key,0);
        }
        this.informationCount.put(ANALYSING_THREADS_KEY,0);
        this.informationCount.put(DOWNLOADING_THREADS_KEY,0);
        this.informationCount.put(URL_KEY,0);

        this.monitorFile = new File(Configuration.getMonitorFilePath());
        try {
            this.monitorFile.createNewFile();
        }catch(IOException e) {
            this.logger.error("Terminating " + e);
            SessionControl.stop();
        }
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
        }else if(monitorInfo.getResourceKey().equals(URL_KEY)) {
            if(monitorInfo.isStarting()) {
                this.analysisStarted();
            }else{
                this.analysisEnded(monitorInfo.getResourceKey());
            }
        }
    }

    private void write() {

        // creates a FileWriter Object
        FileWriter writer;
        try {
            writer = new FileWriter(this.monitorFile);


            int totalThreads = this.informationCount.get(ANALYSING_THREADS_KEY) + this.informationCount.get(DOWNLOADING_THREADS_KEY);
            writer.write("Threads running : " + totalThreads + "\n");
            writer.write("---Analyzing : " + this.informationCount.get(ANALYSING_THREADS_KEY) + "\n");
            writer.write("---Downloading : " + this.informationCount.get(DOWNLOADING_THREADS_KEY) + "\n");
            int totalResources = 0;
            for(String key : DOWNLOAD_TAGS.keySet()) {
                totalResources += this.informationCount.get(key);
            }
            writer.write("Analyzed URLS : " + this.informationCount.get("url") + "\n");
            writer.write("Downloaded resources : " + totalResources + "\n");
            for(String key : DOWNLOAD_TAGS.keySet()) {
                writer.write("---" + key + " : " + this.informationCount.get(key) + "\n");
            }

            writer.flush();
            writer.close();

        }catch (IOException e ){
            this.logger.error("Terminating " + e);
            SessionControl.stop();
        }


    }


    public void run() {
        this.logger.info("Started");
        while(SessionControl.shouldRun()) {
            try {
                MonitorInfo monitorInfo = this.incomingInfoQueue.poll(Configuration.getTimeout(), TimeUnit.MILLISECONDS);
                if(monitorInfo == null) {
                    continue;
                }
                this.update(monitorInfo);
                this.write();
            }catch(InterruptedException e){
                this.logger.error("Exiting " + e);
                break;
            }
        }
        this.logger.info("Finished");
    }
}
