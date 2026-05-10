package model;

import model.base.ItemPerpustakaan;

/**
 * Kelas untuk merepresentasikan buku digital/e-book di perpustakaan.
 * Turunan dari ItemPerpustakaan. Stok tetap 1 (single-user license).
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
                       Kategori kategori, String penerbit, String penulis,
                       double ukuranFile, String format) {
        super(id, judul, tahunTerbit, kategori, penerbit, penulis, 1);
        this.ukuranFile = ukuranFile;
        this.format = format;
    }

    // === Getter & Setter ===
    public double getUkuranFile() { return ukuranFile; }
    public void setUkuranFile(double ukuranFile) { this.ukuranFile = ukuranFile; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

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
