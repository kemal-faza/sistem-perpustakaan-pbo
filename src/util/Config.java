package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton untuk memuat konfigurasi dari file config.properties.
 * Menerapkan Singleton Pattern.
 *
 * Fallback behavior:
 * - Jika config.properties ditemukan di root project, baca dari situ
 * - Jika tidak, gunakan default hardcoded (sebagai safe fallback)
 */
public class Config {

    private static Config instance;
    private Properties props;

    /** Constructor private — singleton */
    private Config() {
        props = new Properties();
        boolean loaded = false;

        // Coba baca dari root project
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            loaded = true;
        } catch (IOException e) {
            // Fallback: baca dari classpath
            try (InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (input != null) {
                    props.load(input);
                    loaded = true;
                }
            } catch (IOException ignored) {}
        }

        if (!loaded) {
            System.err.println("Config: config.properties tidak ditemukan. Menggunakan default.");
            setDefaults();
        }
    }

    /**
     * Mendapatkan instance tunggal Config.
     * Thread-safe via synchronized.
     */
    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    /** Set nilai default (fallback jika file tidak ada) */
    private void setDefaults() {
        props.setProperty("admin.username", "admin");
        props.setProperty("admin.password", "admin123");
    }

    /** Mendapatkan username admin dari konfigurasi */
    public String getAdminUsername() {
        return props.getProperty("admin.username", "admin");
    }

    /** Mendapatkan password admin dari konfigurasi */
    public String getAdminPassword() {
        return props.getProperty("admin.password", "admin123");
    }
}
