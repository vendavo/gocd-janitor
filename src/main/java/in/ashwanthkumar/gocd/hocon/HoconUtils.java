package in.ashwanthkumar.gocd.hocon;

import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.List;

public class HoconUtils {
    public static String getString(Config config, String key, String defaultValue) {
        return config.hasPath(key) ? config.getString(key) : defaultValue;
    }
    public static List<String> getStringListOrEmpty(Config config, String key) {
        return config.hasPath(key) ? config.getStringList(key) : new ArrayList<String>();
    }
    public static boolean getBoolean(Config config, String key, Boolean defaultValue) {
        return config.hasPath(key) ? config.getBoolean(key) : defaultValue;
    }
    public static int getInteger(Config config, String key, int defaultValue) {
        return config.hasPath(key) ? config.getInt(key) : 0;
    }
}
