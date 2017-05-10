package com.rainbow.manager.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by xuming on 2017/5/8.
 */
public class ServiceStorage {

    private String path = null;
    private Properties properties = new Properties();

    private ServiceStorage(String path) {
        if (new File(path).exists()) {
            try {
                properties.load(new FileInputStream(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ServiceStorage getStorage(String path) {
        return new ServiceStorage(path);
    }

    public String get(String key) {
        return (String) properties.get(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void close() throws IOException {
        if (properties != null) {
            FileOutputStream fos = new FileOutputStream(path);
            properties.store(fos, "");

            fos.close();
        }
    }

}
