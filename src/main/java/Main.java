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
        scriptLoader.loadScript("/home/luciano/Documents/Taller3/load_test_console/src/test/resources/validScript.json");
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        UserGenerator userGenerator = new UserGenerator(scriptLoader.getUserCreationPattern(),queue);
        UserLauncher userLauncher = new UserLauncher(scriptLoader.getActionList(),queue);

        new Thread(userGenerator).start();
        new Thread(userLauncher).start();


    }
}
