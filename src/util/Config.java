package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Singleton untuk memuat konfigurasi dari file config.properties.
 * Jika file tidak ditemukan, aplikasi akan terminasi dengan error.
 * Copy config.properties.example menjadi config.properties untuk memulai.
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
            throw new RuntimeException("FATAL: config.properties tidak ditemukan!\n"
                + "Copy config.properties.example menjadi config.properties\n"
                + "dan sesuaikan nilainya sebelum menjalankan aplikasi.");
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

    /** Mendapatkan username admin dari konfigurasi */
    public String getAdminUsername() {
        return props.getProperty("admin.username", "admin");
    }

    /** Mendapatkan password admin dari konfigurasi */
    public String getAdminPassword() {
        String pass = props.getProperty("admin.password");
        if (pass == null || pass.equals("CHANGE_ME")) {
            throw new IllegalStateException(
                "Password admin tidak dikonfigurasi di config.properties.\n"
                + "Copy config.properties.example dan ganti CHANGE_ME dengan password.");
        }
        return pass;
    }
}
