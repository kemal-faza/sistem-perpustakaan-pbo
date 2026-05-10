package model;

import interfaces.Identifiable;

/**
 * Kelas untuk merepresentasikan anggota perpustakaan.
 * Menerapkan enkapsulasi dengan private fields dan public getter/setter.
 */
public class Anggota implements Identifiable {

    /** Batas maksimal peminjaman untuk setiap anggota */
    public static final int MAX_PINJAM = 3;

    private String id;
    private String nama;
    private int pinjamanAktif;

    /** Constructor default */
    public Anggota() {
        this.pinjamanAktif = 0;
    }

    /** Constructor dengan parameter */
    public Anggota(String id, String nama) {
        this.id = id;
        this.nama = nama;
        this.pinjamanAktif = 0;
    }

    // === Getter & Setter ===
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public int getPinjamanAktif() { return pinjamanAktif; }
    public void setPinjamanAktif(int pinjamanAktif) { this.pinjamanAktif = pinjamanAktif; }

    /**
     * Menambah jumlah pinjaman aktif.
     * @throws IllegalStateException jika sudah mencapai batas
     */
    public void tambahPinjaman() {
        if (pinjamanAktif >= MAX_PINJAM) {
            throw new IllegalStateException(
                "Anggota " + nama + " sudah mencapai batas pinjaman (" + MAX_PINJAM + ").");
        }
        pinjamanAktif++;
    }

    /** Mengurangi jumlah pinjaman aktif */
    public void kurangiPinjaman() {
        if (pinjamanAktif > 0) {
            pinjamanAktif--;
        }
    }

    /** Memeriksa apakah anggota masih bisa meminjam */
    public boolean bisaPinjam() {
        return pinjamanAktif < MAX_PINJAM;
    }

    @Override
    public String toString() {
        return id + " - " + nama + " | Pinjaman: " + pinjamanAktif + "/" + MAX_PINJAM;
    }
}
