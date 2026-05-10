package model.abstracts;

import interfaces.IBorrowable;
import interfaces.ISearchable;
import interfaces.Identifiable;

/**
 * Abstract class yang menjadi basis untuk semua item di perpustakaan.
 * Menerapkan konsep abstract class, abstract method, dan interface.
 */
public abstract class ItemPerpustakaan implements Identifiable, IBorrowable, ISearchable {

    // === Atribut dengan Enkapsulasi (private) ===
    private String id;
    private String judul;
    private int tahunTerbit;
    private String kategori;
    private boolean tersedia;
    private String penerbit;

    // === Constructor (Overloading) ===

    /** Constructor default */
    public ItemPerpustakaan() {
        this.tersedia = true;
    }

    /** Constructor dengan parameter */
    public ItemPerpustakaan(String id, String judul, int tahunTerbit,
                            String kategori, String penerbit) {
        this.id = id;
        this.judul = judul;
        this.tahunTerbit = tahunTerbit;
        this.kategori = kategori;
        this.penerbit = penerbit;
        this.tersedia = true;
    }

    // === Getter & Setter (Enkapsulasi) ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public int getTahunTerbit() { return tahunTerbit; }
    public void setTahunTerbit(int tahunTerbit) { this.tahunTerbit = tahunTerbit; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public boolean isTersedia() { return tersedia; }
    public void setTersedia(boolean tersedia) { this.tersedia = tersedia; }

    public String getPenerbit() { return penerbit; }
    public void setPenerbit(String penerbit) { this.penerbit = penerbit; }

    // === Abstract Method (harus diimplementasi subclass) ===
    public abstract double hitungDenda(int hariTerlambat);
    public abstract String getTipe();

    // === Implementasi Method Interface ===

    @Override
    public void pinjam() {
        if (!tersedia) {
            System.out.println("Item '" + judul + "' sedang tidak tersedia.");
            return;
        }
        this.tersedia = false;
        System.out.println("Item '" + judul + "' berhasil dipinjam.");
    }

    @Override
    public void kembalikan() {
        this.tersedia = true;
        System.out.println("Item '" + judul + "' berhasil dikembalikan.");
    }

    @Override
    public void perpanjang() {
        if (tersedia) {
            System.out.println("Item '" + judul + "' sedang tidak dipinjam.");
            return;
        }
        System.out.println("Masa pinjam item '" + judul + "' diperpanjang.");
    }

    @Override
    public boolean cocok(String keyword) {
        if (keyword == null || keyword.isEmpty()) return false;
        String lower = keyword.toLowerCase();
        return judul.toLowerCase().contains(lower)
            || kategori.toLowerCase().contains(lower)
            || penerbit.toLowerCase().contains(lower)
            || id.toLowerCase().contains(lower);
    }

    // === Override Method dari Object ===
    @Override
    public String toString() {
        return "[" + getTipe() + "] " + id + " - " + judul
            + " (" + tahunTerbit + ") | " + (tersedia ? "Tersedia" : "Dipinjam");
    }
}
