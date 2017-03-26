import exception.InvalidScriptException;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.concurrent.*;

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

        Thread userGeneratorThread = new Thread(userGenerator);
        Thread userLauncherThread = new Thread(userLauncher);
        Thread reporterThread = new Thread(reporter);
        Thread monitorThread = new Thread(monitor);

        userGeneratorThread.start();
        userLauncherThread.start();
        reporterThread.start();
        monitorThread.start();


        TimeUnit.MILLISECONDS.sleep(5000);
        SessionControl.stop();
        userGeneratorThread.join();
        userLauncherThread.join();
        reporterThread.join();
        monitorThread.join();

        System.out.println("END!!!!");


    }
}
