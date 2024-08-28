package com.sociallibparser.singelton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReaderSingelton {
    private static ConfigReaderSingelton INSTANCE;
    private final Properties properties = new Properties();

    public ConfigReaderSingelton() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public synchronized static ConfigReaderSingelton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigReaderSingelton();
        }
        return INSTANCE;
    }
}
