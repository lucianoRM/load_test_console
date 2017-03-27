package reports;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luciano on 25/03/17.
 */
public class MonitorInfo {


    private String resourceKey;
    private boolean isStarting;

    public MonitorInfo() {}


    public void notifyActionStarted(String tag) {
        this.resourceKey = tag;
        this.isStarting = true;
    }

    public void notifyActionEnded(String tag) {
        this.resourceKey = tag;
        this.isStarting = false;
    }

    public String getResourceKey() {
        return this.resourceKey;
    }

    public boolean isStarting() {
        return this.isStarting;
    }

}
