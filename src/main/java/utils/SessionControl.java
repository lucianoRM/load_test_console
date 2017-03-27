package utils;

/**
 * Created by luciano on 25/03/17.
 */
public class SessionControl {

    private static boolean shouldRun = true;


    public static synchronized boolean shouldRun() {
        return shouldRun;
    }

    public static synchronized void stop() {
        shouldRun = false;
    }
}
