package model;

import interfaces.Identifiable;
import util.Config;

/**
 * Kelas untuk merepresentasikan admin perpustakaan.
 * Admin memiliki kredensial untuk login dan mengelola sistem.
 */
public class Admin implements Identifiable {

    private String id;
    private String username;
    private String password;

    /** Constructor default — membaca credential dari Config */
    public Admin() {
        this.id = "ADM001";
        Config config = Config.getInstance();
        this.username = config.getAdminUsername();
        this.password = config.getAdminPassword();
    }

    /** Constructor dengan parameter */
    public Admin(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // === Getter & Setter ===
    @Override
    public String getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /**
     * Memvalidasi password yang dimasukkan.
     * @param inputPassword password yang akan divalidasi
     * @return true jika password cocok
     */
    public boolean validatePassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    @Override
    public String toString() {
        return "Admin: " + username + " (" + id + ")";
    }
}
