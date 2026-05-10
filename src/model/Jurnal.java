package model;

import model.base.ItemPerpustakaan;

/**
 * Kelas untuk merepresentasikan jurnal ilmiah di perpustakaan.
 * Turunan dari ItemPerpustakaan. Jurnal bersifat fisik, memiliki stok.
 */
public class Jurnal extends ItemPerpustakaan {

    private int volume;
    private int nomor;
    private String bidang;

    /** Constructor default */
    public Jurnal() {
        super();
    }

    /** Constructor dengan parameter */
    public Jurnal(String id, String judul, int tahunTerbit,
                  Kategori kategori, String penerbit, String penulis,
                  int volume, int nomor, String bidang, int stok) {
        super(id, judul, tahunTerbit, kategori, penerbit, penulis, stok);
        this.volume = volume;
        this.nomor = nomor;
        this.bidang = bidang;
    }

    // === Getter & Setter ===
    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    public int getNomor() { return nomor; }
    public void setNomor(int nomor) { this.nomor = nomor; }

    public String getBidang() { return bidang; }
    public void setBidang(String bidang) { this.bidang = bidang; }

    @Override
    public double hitungDenda(int hariTerlambat) {
        return hariTerlambat * 2000;
    }

    @Override
    public String getTipe() {
        return "Jurnal";
    }

    @Override
    public String toString() {
        return super.toString() + " | Vol." + volume + " No." + nomor + " | Bidang: " + bidang;
    }
}
