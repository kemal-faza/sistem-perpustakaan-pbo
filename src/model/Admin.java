package model;

/**
 * Kelas untuk merepresentasikan admin perpustakaan.
 * Admin memiliki kredensial untuk login dan mengelola sistem.
 */
public class Admin {

    /** Username default untuk admin */
    public static final String DEFAULT_USERNAME = "admin";
    /** Password default untuk admin */
    public static final String DEFAULT_PASSWORD = "admin123";

    private String username;
    private String password;

    /** Constructor default dengan kredensial default */
    public Admin() {
        this.username = DEFAULT_USERNAME;
        this.password = DEFAULT_PASSWORD;
    }

    /** Constructor dengan parameter */
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // === Getter & Setter ===
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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
        return "Admin: " + username;
    }
}
