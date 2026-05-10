package model;

import interfaces.Identifiable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Kelas untuk merepresentasikan transaksi peminjaman item perpustakaan.
 * Menerapkan konsep Composition (mengandung reference ke ItemPerpustakaan dan Anggota).
 */
public class Peminjaman implements Identifiable {

    private String idPeminjaman;
    private String idAnggota;      // Association ke Anggota
    private String idItem;         // Association ke ItemPerpustakaan
    private LocalDate tanggalPinjam;
    private LocalDate tanggalKembali;       // Estimasi tanggal kembali
    private LocalDate tanggalDikembalikan;  // Tanggal aktual dikembalikan (null jika belum)
    private StatusPeminjaman status;
    private double denda;
    private int jumlahPerpanjang;

    /** Maksimal perpanjangan yang diizinkan */
    public static final int MAX_PERPANJANG = 2;

    /** Constructor default */
    public Peminjaman() {
        this.status = StatusPeminjaman.DIPINJAM;
        this.denda = 0.0;
    }

    /** Constructor dengan parameter */
    public Peminjaman(String idPeminjaman, String idAnggota, String idItem,
                      LocalDate tanggalPinjam, LocalDate tanggalKembali) {
        this.idPeminjaman = idPeminjaman;
        this.idAnggota = idAnggota;
        this.idItem = idItem;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalKembali = tanggalKembali;
        this.tanggalDikembalikan = null;
        this.status = StatusPeminjaman.DIPINJAM;
        this.denda = 0.0;
        this.jumlahPerpanjang = 0;
    }

    // === Getter & Setter ===
    @Override
    public String getId() { return idPeminjaman; }

    public String getIdPeminjaman() { return idPeminjaman; }
    public void setIdPeminjaman(String idPeminjaman) { this.idPeminjaman = idPeminjaman; }

    public String getIdAnggota() { return idAnggota; }
    public void setIdAnggota(String idAnggota) { this.idAnggota = idAnggota; }

    public String getIdItem() { return idItem; }
    public void setIdItem(String idItem) { this.idItem = idItem; }

    public LocalDate getTanggalPinjam() { return tanggalPinjam; }
    public void setTanggalPinjam(LocalDate tanggalPinjam) { this.tanggalPinjam = tanggalPinjam; }

    public LocalDate getTanggalKembali() { return tanggalKembali; }
    public void setTanggalKembali(LocalDate tanggalKembali) { this.tanggalKembali = tanggalKembali; }

    public LocalDate getTanggalDikembalikan() { return tanggalDikembalikan; }
    public void setTanggalDikembalikan(LocalDate tanggalDikembalikan) {
        this.tanggalDikembalikan = tanggalDikembalikan;
    }

    public StatusPeminjaman getStatus() { return status; }
    public void setStatus(StatusPeminjaman status) { this.status = status; }

    public double getDenda() { return denda; }
    public void setDenda(double denda) { this.denda = denda; }

    /**
     * Menghitung denda berdasarkan selisih hari.
     * @return jumlah denda (belum dikalikan tarif per hari item)
     */
    public int hitungHariTerlambat() {
        if (tanggalDikembalikan != null && tanggalDikembalikan.isAfter(tanggalKembali)) {
            return (int) ChronoUnit.DAYS.between(tanggalKembali, tanggalDikembalikan);
        }
        if (tanggalDikembalikan == null && LocalDate.now().isAfter(tanggalKembali)) {
            return (int) ChronoUnit.DAYS.between(tanggalKembali, LocalDate.now());
        }
        return 0;
    }

    public int getJumlahPerpanjang() { return jumlahPerpanjang; }

    /** Memperpanjang masa pinjam (3 hari), maksimal 2× */
    public void perpanjang() {
        if (status != StatusPeminjaman.DIPINJAM) {
            throw new IllegalStateException(
                "Peminjaman #" + idPeminjaman + " sudah selesai, tidak bisa diperpanjang.");
        }
        if (jumlahPerpanjang >= MAX_PERPANJANG) {
            throw new IllegalStateException(
                "Peminjaman #" + idPeminjaman + " sudah mencapai batas perpanjangan ("
                + MAX_PERPANJANG + "×).");
        }
        this.tanggalKembali = this.tanggalKembali.plusDays(3);
        this.jumlahPerpanjang++;
    }

    /**
     * Mengembalikan item (menutup transaksi peminjaman).
     * @return jumlah hari terlambat (0 jika tepat waktu)
     */
    public int kembalikan() {
        if (tanggalDikembalikan != null) {
            throw new IllegalStateException(
                "Peminjaman #" + idPeminjaman + " sudah dikembalikan sebelumnya.");
        }
        this.tanggalDikembalikan = LocalDate.now();
        int hariTerlambat = hitungHariTerlambat();
        if (hariTerlambat > 0) {
            this.status = StatusPeminjaman.TERLAMBAT;
        } else {
            this.status = StatusPeminjaman.DIKEMBALIKAN;
        }
        return hariTerlambat;
    }

    @Override
    public String toString() {
        return "Peminjaman#" + idPeminjaman
            + " | Anggota: " + idAnggota
            + " | Item: " + idItem
            + " | Status: " + status
            + " | Denda: Rp" + String.format("%,.0f", denda);
    }
}
