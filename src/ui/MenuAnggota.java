package ui;

import exception.*;
import interfaces.ILibraryService;
import model.*;
import model.base.ItemPerpustakaan;
import service.PerpustakaanService;

import java.util.List;

/**
 * Menu untuk anggota perpustakaan.
 * Menangani pencarian, peminjaman, pengembalian, dan riwayat.
 */
public class MenuAnggota extends MenuManager {

    private static final String[] MENU_ANGGOTA = {
        "Logout",
        "Cari Buku",
        "Pinjam Buku",
        "Kembalikan Buku",
        "Riwayat Peminjaman",
        "Perpanjang Peminjaman"
    };

    private ILibraryService service;

    public MenuAnggota() {
        this.service = PerpustakaanService.getInstance();
    }

    /** Menjalankan loop menu anggota */
    public void jalankan() {
        boolean running = true;
        while (running) {
            tampilkanHeader("MENU ANGGOTA");

            Anggota current = service.getCurrentAnggota();
            cetakInfo("Login sebagai: " + current.getNama()
                + " | Pinjaman: " + current.getPinjamanAktif() + "/" + Anggota.MAX_PINJAM);

            int pilihan = tampilkanMenu(MENU_ANGGOTA);

            try {
                switch (pilihan) {
                    case 1 -> cariBuku();
                    case 2 -> pinjamBuku();
                    case 3 -> kembalikanBuku();
                    case 4 -> riwayatPeminjaman();
                    case 5 -> perpanjangPeminjaman();
                    case 0 -> {
                        cetakInfo("Logout berhasil.");
                        service.logout();
                        running = false;
                    }
                    default -> cetakError("Pilihan tidak valid.");
                }
            } catch (Exception e) {
                cetakError("Terjadi kesalahan: " + e.getMessage());
            }

            if (running && pilihan != 0) {
                tungguEnter();
            }
        }
    }

    // ========== BUKU ==========

    private void cariBuku() {
        super.cariBuku(service, true);
    }

    // ========== PEMINJAMAN ==========

    private void pinjamBuku() {
        tampilkanHeader("PINJAM BUKU");

        Anggota anggota = service.getCurrentAnggota();
        if (anggota == null) {
            cetakError("Session anggota tidak valid. Silakan login ulang.");
            return;
        }

        String idBuku = bacaInput("ID Buku");

        // Cek info buku dulu
        ItemPerpustakaan item = service.getBukuById(idBuku);
        if (item == null) {
            cetakError("Item dengan ID '" + idBuku + "' tidak ditemukan.");
            return;
        }
        cetakInfo("Meminjam: " + item.getJudul() + " (" + item.getTipe() + ")");
        String konfirmasi = bacaInput("Yakin pinjam? (y/n)");
        if (!konfirmasi.equalsIgnoreCase("y")) {
            cetakInfo("Peminjaman dibatalkan.");
            return;
        }

        try {
            service.pinjamBuku(idBuku, anggota.getId());
            cetakSukses("Buku berhasil dipinjam! Masa pinjam 7 hari.");
        } catch (BukuTidakDitemukanException | AnggotaTidakValidException
                | BukuTidakTersediaException | PeminjamanMelebihiBatasException e) {
            cetakError(e.getMessage());
        }
    }

    private void kembalikanBuku() {
        tampilkanHeader("KEMBALIKAN BUKU");

        // Tampilkan peminjaman aktif anggota ini
        Anggota anggota = service.getCurrentAnggota();
        List<Peminjaman> aktif = service.getPeminjamanAktif()
            .stream()
            .filter(p -> p.getIdAnggota().equals(anggota.getId()))
            .toList();

        if (aktif.isEmpty()) {
            cetakInfo("Tidak ada peminjaman aktif.");
            return;
        }

        cetakInfo("Peminjaman aktif:");
        for (Peminjaman p : aktif) {
            ItemPerpustakaan item = service.getBukuById(p.getIdItem());
            String judul = (item != null) ? item.getJudul() : p.getIdItem();
            int hariTerlambat = p.hitungHariTerlambat();
            if (hariTerlambat > 0) {
                cetakInfo("  #" + p.getIdPeminjaman() + " | " + judul
                    + " | Batas: " + p.getTanggalKembali() + " | ⚠ Terlambat " + hariTerlambat + " hari");
            } else {
                cetakInfo("  #" + p.getIdPeminjaman() + " | " + judul
                    + " | Batas: " + p.getTanggalKembali());
            }
        }

        String idPeminjaman = bacaInput("\nID Peminjaman yang dikembalikan");

        try {
            Peminjaman updated = service.kembalikanBuku(idPeminjaman);
            if (updated.getDenda() > 0) {
                cetakInfo("Terlambat! Denda: Rp " + String.format("%,.0f", updated.getDenda()));
            } else {
                cetakSukses("Buku berhasil dikembalikan tepat waktu. Tidak ada denda.");
            }
        } catch (BukuTidakDitemukanException | AnggotaTidakValidException | IllegalStateException e) {
            cetakError(e.getMessage());
        }
    }

    private void riwayatPeminjaman() {
        tampilkanHeader("RIWAYAT PEMINJAMAN");

        Anggota anggota = service.getCurrentAnggota();
        if (anggota == null) {
            cetakError("Session anggota tidak valid. Silakan login ulang.");
            return;
        }

        List<Peminjaman> riwayat = service.getRiwayatPeminjaman(anggota.getId());

        if (riwayat.isEmpty()) {
            cetakInfo("Belum ada riwayat peminjaman.");
            return;
        }

        cetakInfo("Total " + riwayat.size() + " transaksi:\n");
        for (Peminjaman p : riwayat) {
            ItemPerpustakaan item = service.getBukuById(p.getIdItem());
            String judul = (item != null) ? item.getJudul() : p.getIdItem();

            String statusLabel;
            String infoTanggal;
            switch (p.getStatus()) {
                case DIPINJAM -> {
                    statusLabel = "Dipinjam";
                    infoTanggal = " | Batas: " + p.getTanggalKembali();
                }
                case DIKEMBALIKAN -> {
                    statusLabel = "Dikembalikan";
                    infoTanggal = " | Dikembalikan: "
                        + (p.getTanggalDikembalikan() != null ? p.getTanggalDikembalikan() : "-");
                }
                case TERLAMBAT -> {
                    statusLabel = "Terlambat";
                    infoTanggal = " | Jatuh Tempo: " + p.getTanggalKembali()
                        + " | Kembali: "
                        + (p.getTanggalDikembalikan() != null ? p.getTanggalDikembalikan() : "-");
                }
                default -> {
                    statusLabel = p.getStatus().toString();
                    infoTanggal = "";
                }
            }

            System.out.println("  #" + p.getIdPeminjaman()
                + " | " + judul
                + " | Pinjam: " + p.getTanggalPinjam()
                + infoTanggal
                + " | Status: " + statusLabel
                + (p.getDenda() > 0 ? " | Denda: Rp" + String.format("%,.0f", p.getDenda()) : ""));
        }
    }

    private void perpanjangPeminjaman() {
        tampilkanHeader("PERPANJANG PEMINJAMAN");

        // Tampilkan daftar peminjaman aktif + counter perpanjang
        Anggota anggota = service.getCurrentAnggota();
        if (anggota == null) {
            cetakError("Session anggota tidak valid. Silakan login ulang.");
            return;
        }

        List<Peminjaman> aktif = service.getPeminjamanAktif().stream()
                .filter(p -> p.getIdAnggota().equals(anggota.getId())).toList();

        if (aktif.isEmpty()) {
            cetakInfo("Tidak ada peminjaman aktif.");
            return;
        }

        cetakInfo("Peminjaman aktif:");
        for (Peminjaman p : aktif) {
            ItemPerpustakaan item = service.getBukuById(p.getIdItem());
            String judul = (item != null) ? item.getJudul() : p.getIdItem();
            cetakInfo("#" + p.getIdPeminjaman() + " | " + judul
                + " | Perpanjang: " + p.getJumlahPerpanjang() + "/" + Peminjaman.MAX_PERPANJANG
                + " | Batas: " + p.getTanggalKembali());
        }

        String idPeminjaman = bacaInput("\nID Peminjaman yang ingin diperpanjang");

        // Validasi ownership: gunakan findById langsung (tanpa loop)
        Peminjaman target = service.getPeminjamanById(idPeminjaman);
        if (target == null) {
            cetakError("Peminjaman dengan ID '" + idPeminjaman + "' tidak ditemukan.");
            return;
        }
        if (!target.getIdAnggota().equals(anggota.getId())) {
            cetakError("Peminjaman #" + idPeminjaman + " bukan milik Anda.");
            return;
        }

        try {
            service.perpanjangPeminjaman(idPeminjaman);
            cetakSukses("Peminjaman #" + idPeminjaman + " diperpanjang 3 hari.");
        } catch (BukuTidakDitemukanException | IllegalStateException e) {
            cetakError(e.getMessage());
        }
    }
}
