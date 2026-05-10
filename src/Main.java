import exception.AnggotaTidakValidException;
import service.PerpustakaanService;
import ui.MenuAdmin;
import ui.MenuAnggota;
import ui.MenuManager;

/**
 * Entry point aplikasi Sistem Manajemen Perpustakaan.
 * Menangani login flow dan dispatch ke menu admin/anggota.
 */
public class Main {

    private static final String[] MENU_UTAMA = {
        "Keluar",
        "Login Admin",
        "Login Anggota"
    };

    public static void main(String[] args) {
        PerpustakaanService service = PerpustakaanService.getInstance();

        // Load sample data jika database kosong
        service.loadSampleData();

        System.out.println("\n" + "=".repeat(43));
        System.out.println("  SISTEM MANAJEMEN PERPUSTAKAAN");
        System.out.println("  Tugas Besar Pemrograman Berorientasi Objek");
        System.out.println("=".repeat(43));

        MenuManager menuBase = new MenuManager() {};
        boolean running = true;

        while (running) {
            int pilihan = menuBase.tampilkanMenu(MENU_UTAMA);

            switch (pilihan) {
                case 1 -> loginAdmin(service);
                case 2 -> loginAnggota(service);
                case 0 -> {
                    System.out.println("\nTerima kasih telah menggunakan aplikasi ini.");
                    running = false;
                }
                default -> menuBase.cetakError("Pilihan tidak valid.");
            }
        }
    }

    private static void loginAdmin(PerpustakaanService service) {
        MenuManager menu = new MenuManager() {};
        menu.tampilkanHeader("LOGIN ADMIN");

        String username = menu.bacaInput("Username");
        String password = menu.bacaInput("Password");

        if (service.loginAdmin(username, password)) {
            menu.cetakSukses("Login berhasil sebagai Admin.");
            new MenuAdmin().jalankan();
        } else {
            menu.cetakError("Username atau password salah.");
            menu.tungguEnter();
        }
    }

    private static void loginAnggota(PerpustakaanService service) {
        MenuManager menu = new MenuManager() {};
        menu.tampilkanHeader("LOGIN ANGGOTA");

        String id = menu.bacaInput("ID Anggota");

        try {
            if (service.loginAnggota(id)) {
                menu.cetakSukses("Selamat datang, " + service.getCurrentAnggota().getNama() + "!");
                new MenuAnggota().jalankan();
            }
        } catch (AnggotaTidakValidException e) {
            menu.cetakError(e.getMessage());
            menu.tungguEnter();
        }
    }
}
