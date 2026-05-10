package model;

import model.abstracts.ItemPerpustakaan;

/**
 * Kelas untuk merepresentasikan buku digital/e-book di perpustakaan.
 * Turunan dari ItemPerpustakaan.
 */
public class BukuDigital extends ItemPerpustakaan {

    private double ukuranFile; // dalam MB
    private String format; // PDF, EPUB, dll

    /** Constructor default */
    public BukuDigital() {
        super();
    }

    /** Constructor dengan parameter */
    public BukuDigital(String id, String judul, int tahunTerbit,
            String kategori, String penerbit,
            double ukuranFile, String format) {
        super(id, judul, tahunTerbit, kategori, penerbit);
        this.ukuranFile = ukuranFile;
        this.format = format;
    }

    // === Getter & Setter ===
    public double getUkuranFile() {
        return ukuranFile;
    }

    public void setUkuranFile(double ukuranFile) {
        this.ukuranFile = ukuranFile;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Menghitung denda keterlambatan untuk buku digital.
     * Denda lebih murah: Rp 500 per hari (karena tidak ada biaya fisik).
     */
    @Override
    public double hitungDenda(int hariTerlambat) {
        return hariTerlambat * 500;
    }

    @Override
    public String getTipe() {
        return "Buku Digital";
    }

    @Override
    public String toString() {
        return super.toString() + " | " + ukuranFile + "MB | Format: " + format;
    }
}
