import exception.InvalidScriptException;

import java.io.FileNotFoundException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by luciano on 18/03/17.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException, InvalidScriptException, FileNotFoundException{

        ScriptLoader scriptLoader = new ScriptLoader();
        scriptLoader.loadScript("./src/test/resources/validScript.json");
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        BlockingQueue<ActionInfo> actionInfoBlockingQueue = new LinkedBlockingQueue<>();
        BlockingQueue<MonitorInfo> monitorInfoBlockingQueue = new LinkedBlockingQueue<>();
        Monitor monitor = new Monitor(monitorInfoBlockingQueue);
        UserGenerator userGenerator = new UserGenerator(scriptLoader.getUserCreationPattern(),queue);
        UserLauncher userLauncher = new UserLauncher(scriptLoader.getActionList(),queue,actionInfoBlockingQueue,monitorInfoBlockingQueue);
        Reporter reporter = new Reporter(actionInfoBlockingQueue);

        new Thread(userGenerator).start();
        new Thread(userLauncher).start();
        new Thread(reporter).start();
        new Thread(monitor).start();


    }
}
