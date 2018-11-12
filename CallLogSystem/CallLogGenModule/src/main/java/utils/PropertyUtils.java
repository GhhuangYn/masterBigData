package utils;

import javafx.beans.property.Property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/12 10:18
 */
public class PropertyUtils {

    private static Properties props;

    static {
        InputStream inputStream =
                ClassLoader.getSystemResourceAsStream("classpath:.gendata.conf");
        props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getInt(String key) {
        return Integer.valueOf(props.getProperty(key));
    }

    public static String getString(String key) {
        return props.getProperty(key);
    }
}
