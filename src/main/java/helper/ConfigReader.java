package helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads key/value pairs from config/config.properties.
 * Loaded once (static initializer) and reused across the run.
 *
 * System properties (passed via -Dkey=value on the Maven/Java command line)
 * always take precedence over the file, so CI pipelines / scheduled tasks
 * can override values (e.g. credentials, headless flag) without editing
 * the checked-in file.
 */
public class ConfigReader {

    private static final Properties properties = new Properties();
    private static final String CONFIG_PATH = "config" + java.io.File.separator + "config.properties";

    static {
        try (var fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config file at " + CONFIG_PATH, e);
        }
    }

    private ConfigReader() {
        // utility class - no instances
    }

    /**
     * Fetches a property value. Checks JVM system properties first
     * (-Dkey=value) then falls back to config.properties.
     */
    public static String get(String key) {
        var sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        var value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config.properties");
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        var sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        var value = properties.getProperty(key, defaultValue);
        return value == null ? null : value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
