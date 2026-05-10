package model.base;

import interfaces.IBorrowable;
import interfaces.ISearchable;
import interfaces.Identifiable;
import model.Kategori;

/**
 * Abstract class yang menjadi basis untuk semua item di perpustakaan.
 * Menerapkan konsep abstract class, abstract method, dan interface.
 */
public abstract class ItemPerpustakaan implements Identifiable, IBorrowable, ISearchable {

    // === Atribut dengan Enkapsulasi (private) ===
    private String type;     // Discriminator untuk JSON deserialization
    private String id;
    private String judul;
    private int tahunTerbit;
    private Kategori kategori;
    private String penerbit;
    private String penulis;
    private int stok;
    private int dipinjam;

    // === Constructor (Overloading) ===

    /** Constructor default — stok=1 untuk backward compat JSON */
    public ItemPerpustakaan() {
        this.stok = 1;
        this.dipinjam = 0;
        this.type = getTipe();
    }

    /** Constructor dengan parameter */
    public ItemPerpustakaan(String id, String judul, int tahunTerbit,
                            Kategori kategori, String penerbit, String penulis, int stok) {
        this.id = id;
        this.judul = judul;
        this.tahunTerbit = tahunTerbit;
        this.kategori = kategori;
        this.penerbit = penerbit;
        this.penulis = penulis;
        this.stok = (stok > 0) ? stok : 1;
        this.dipinjam = 0;
        this.type = getTipe();
    }

    // === Getter & Setter (Enkapsulasi) ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public int getTahunTerbit() { return tahunTerbit; }
    public void setTahunTerbit(int tahunTerbit) { this.tahunTerbit = tahunTerbit; }

    public Kategori getKategori() { return kategori; }
    public void setKategori(Kategori kategori) { this.kategori = kategori; }

    public String getPenulis() { return penulis; }
    public void setPenulis(String penulis) { this.penulis = penulis; }

    /** Apakah item tersedia untuk dipinjam? (stok > dipinjam) */
    public boolean isTersedia() { return (stok - dipinjam) > 0; }

    /** Jumlah copy yang tersedia saat ini */
    public int getTersedia() { return stok - dipinjam; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = Math.max(stok, 0); }

    public int getDipinjam() { return dipinjam; }
    public void setDipinjam(int dipinjam) { this.dipinjam = Math.max(dipinjam, 0); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPenerbit() { return penerbit; }
    public void setPenerbit(String penerbit) { this.penerbit = penerbit; }

    // === Abstract Method (harus diimplementasi subclass) ===
    public abstract double hitungDenda(int hariTerlambat);
    public abstract String getTipe();

    // === Implementasi Method Interface ===

    @Override
    public void pinjam() {
        if (!isTersedia()) {
            throw new IllegalStateException(
                "Item '" + judul + "' sedang tidak tersedia (stok: "
                + getTersedia() + "/" + stok + ").");
        }
        this.dipinjam++;
    }

    @Override
    public void kembalikan() {
        if (dipinjam <= 0) {
            throw new IllegalStateException(
                "Item '" + judul + "' sedang tidak dipinjam.");
        }
        this.dipinjam--;
    }

    @Override
    public void perpanjang() {
        if (dipinjam <= 0) {
            throw new IllegalStateException(
                "Item '" + judul + "' sedang tidak dipinjam.");
        }
    }

    @Override
    public boolean cocok(String keyword) {
        if (keyword == null || keyword.isEmpty()) return false;
        String lower = keyword.toLowerCase();
        return judul.toLowerCase().contains(lower)
            || (penulis != null && penulis.toLowerCase().contains(lower))
            || kategori.getDisplayName().toLowerCase().contains(lower)
            || penerbit.toLowerCase().contains(lower)
            || id.toLowerCase().contains(lower);
    }

    // === Override Method dari Object ===
    @Override
    public String toString() {
        return "[" + getTipe() + "] " + id + " - " + judul
            + " oleh " + (penulis != null ? penulis : "-")
            + " (" + tahunTerbit + ")"
            + " | " + kategori.getDisplayName()
            + " | Sisa: " + getTersedia() + "/" + stok;
    }
}
