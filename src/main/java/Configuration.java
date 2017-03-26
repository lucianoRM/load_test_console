import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by luciano on 19/03/17.
 */
public class Configuration {

    private static Properties properties;
    private static final String CONFIGURATION_FILE_NAME = "config.properties";
    private static Logger logger = LogManager.getLogger(Configuration.class);

    static {
        properties = new Properties();
        InputStream is = Configuration.class.getResourceAsStream(CONFIGURATION_FILE_NAME);
        try {
            properties.load(is);
            is.close();
        } catch (IOException e) {
            logger.warn("Error reading from properties file" + e);
            SessionControl.stop();
        }
    }

    public static int getConcurrentUsersCount() {
        return Integer.parseInt(properties.getProperty("concurrentuserscount"));
    }

    public static int getConcurrentDownloadersCount() {
        return Integer.parseInt(properties.getProperty("concurrentdownloderscount"));
    }

    public static int getTimeout() {
        return Integer.parseInt(properties.getProperty("timeout"));
    }


    public static int getReportingTimeSlice() {
        return Integer.parseInt(properties.getProperty("reportingtimeslice"));
    }

    public static String getMonitorFilePath() {
        return properties.getProperty("monitorfilepath");
    }
}
