package org.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = PropertyLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is == null) {
                System.out.println("Can't find application.properties file");
            }

            PROPERTIES.load(is);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static int getServerPort() {
        return Integer.valueOf(
                PROPERTIES.getProperty("server.port", "8081")
        );
    }

    public static int getThreadPoolSize() {
        return Integer.valueOf(
                PROPERTIES.getProperty("server.thread-pool.size", "5")
        );
    }

    public static long getTimeout() {
        return Long.valueOf(
                PROPERTIES.getProperty("server.timout", "500")
        );
    }
}
