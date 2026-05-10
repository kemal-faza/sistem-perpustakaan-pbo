package ui;

import java.util.Scanner;
import java.util.List;
import model.base.ItemPerpustakaan;
import service.PerpustakaanService;

/**
 * Base class untuk menu CLI.
 * Menyediakan utility methods untuk input/output konsol.
 */
public class MenuManager {

    protected static final Scanner scanner = new Scanner(System.in);
    protected static final String LINE = "===========================================";

    /** Menampilkan header dengan judul */
    public void tampilkanHeader(String judul) {
        System.out.println("\n" + LINE);
        System.out.println("  " + judul);
        System.out.println(LINE);
    }

    /**
     * Menampilkan menu dan membaca pilihan user.
     * @param opsi daftar opsi menu (index 0 = judul, index 1..n = opsi)
     * @return nomor opsi yang dipilih (1-based)
     */
    public int tampilkanMenu(String[] opsi) {
        System.out.println();
        for (int i = 1; i < opsi.length; i++) {
            System.out.println("  " + i + ". " + opsi[i]);
        }
        System.out.println("  0. " + opsi[0]);
        System.out.print("Pilih menu: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Membaca input string dari user */
    public String bacaInput(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    /** Membaca input integer dari user */
    public int bacaInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [Error] Masukkan angka yang valid.");
            }
        }
    }

    /** Membaca input double dari user */
    public double bacaDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [Error] Masukkan angka yang valid (contoh: 5.2).");
            }
        }
    }

    /** Membaca input integer positif (> 0) */
    public int bacaIntPositif(String prompt) {
        while (true) {
            int nilai = bacaInt(prompt);
            if (nilai > 0) return nilai;
            System.out.println("  [Error] Masukkan angka positif.");
        }
    }

    /** Membaca input wajib (tidak boleh kosong) */
    public String bacaInputWajib(String prompt) {
        while (true) {
            String input = bacaInput(prompt);
            if (!input.trim().isEmpty()) return input.trim();
            System.out.println("  [Error] " + prompt + " tidak boleh kosong.");
        }
    }

    /** Menunggu user menekan Enter */
    public void tungguEnter() {
        System.out.print("\nTekan Enter untuk melanjutkan...");
        scanner.nextLine();
    }

    /** Cetak pesan sukses (hijau di terminal yang support) */
    public void cetakSukses(String msg) {
        System.out.println("  [OK] " + msg);
    }

    /** Cetak pesan error */
    public void cetakError(String msg) {
        System.out.println("  [Error] " + msg);
    }

    /** Cetak pesan informasi */
    public void cetakInfo(String msg) {
        System.out.println("  " + msg);
    }

    /**
     * Membaca input lokasi rak dengan validasi format.
     * Format yang valid: [Huruf][Angka]-[Slot], contoh: A1-01, B2-15
     */
    public String bacaLokasiRak() {
        while (true) {
            String input = bacaInput("Lokasi Rak (format: A1-01)");
            if (input.isEmpty()) return "";
            String cleaned = input.trim().toUpperCase();
            if (cleaned.matches("^[A-Z]\\d+-\\d+$")) return cleaned;
            cetakError("Format tidak valid. Contoh: A1-01, B2-15");
        }
    }

    /**
     * Method pencarian buku bersama untuk Admin dan Anggota.
     * @param service referensi PerpustakaanService
     * @param tampilStatus jika true, tampilkan status ketersediaan (untuk Anggota)
     */
    public void cariBuku(PerpustakaanService service, boolean tampilStatus) {
        tampilkanHeader("CARI BUKU");

        String keyword = bacaInput("Kata kunci");
        List<ItemPerpustakaan> hasil = service.cariBuku(keyword);

        if (hasil.isEmpty()) {
            cetakInfo("Tidak ada item yang cocok dengan \"" + keyword + "\".");
            return;
        }

        cetakInfo("Ditemukan " + hasil.size() + " item:\n");
        for (ItemPerpustakaan item : hasil) {
            if (tampilStatus) {
                String status = item.isTersedia() ? "✔ Tersedia" : "✖ Dipinjam";
                System.out.println("  " + item + " | " + status);
            } else {
                System.out.println("  " + item);
            }
        }
    }
}
