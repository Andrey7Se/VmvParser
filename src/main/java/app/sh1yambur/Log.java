package app.sh1yambur;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private static Logger logger;

    private Log() {
    }

    public static Logger write() {
        if (logger == null) {
            System.setProperty("log4j2.configurationFile", "log4j2.properties");
            logger = LogManager.getLogger();
        }

        return logger;
    }
}
