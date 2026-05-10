package model;

import model.abstracts.ItemPerpustakaan;

/**
 * Kelas untuk merepresentasikan buku fisik di perpustakaan.
 * Turunan dari ItemPerpustakaan.
 */
public class BukuFisik extends ItemPerpustakaan {

    private int jumlahHalaman;
    private String lokasiRak;

    /** Constructor default */
    public BukuFisik() {
        super();
    }

    /** Constructor dengan parameter */
    public BukuFisik(String id, String judul, int tahunTerbit,
                     String kategori, String penerbit,
                     int jumlahHalaman, String lokasiRak) {
        super(id, judul, tahunTerbit, kategori, penerbit);
        this.jumlahHalaman = jumlahHalaman;
        this.lokasiRak = lokasiRak;
    }

    // === Getter & Setter ===
    public int getJumlahHalaman() { return jumlahHalaman; }
    public void setJumlahHalaman(int jumlahHalaman) { this.jumlahHalaman = jumlahHalaman; }

    public String getLokasiRak() { return lokasiRak; }
    public void setLokasiRak(String lokasiRak) { this.lokasiRak = lokasiRak; }

    /**
     * Menghitung denda keterlambatan untuk buku fisik.
     * Denda: Rp 1000 per hari.
     */
    @Override
    public double hitungDenda(int hariTerlambat) {
        return hariTerlambat * 1000;
    }

    @Override
    public String getTipe() {
        return "Buku Fisik";
    }

    @Override
    public String toString() {
        return super.toString() + " | Hal: " + jumlahHalaman + " | Rak: " + lokasiRak;
    }
}
