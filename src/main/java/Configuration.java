import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by luciano on 19/03/17.
 */
public class Configuration {

    private static Properties properties;
    private static final String CONFIGURATION_FILE_NAME = "config.properties";

    static {
        properties = new Properties();
        InputStream is = Configuration.class.getResourceAsStream(CONFIGURATION_FILE_NAME);
        try {
            properties.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getConcurrentUsersCount() {
        return Integer.parseInt(properties.getProperty("concurrentuserscount"));
    }

    public static int getConcurrentDownloadersCount() {
        return Integer.parseInt(properties.getProperty("concurrentdownloaderscount"));
    }

}
