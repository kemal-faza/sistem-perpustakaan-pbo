package ui;

import exception.*;
import model.*;
import model.base.ItemPerpustakaan;
import service.PerpustakaanService;

import java.util.List;

/**
 * Menu untuk admin perpustakaan.
 * Menangani CRUD buku, manajemen anggota, dan laporan.
 */
public class MenuAdmin extends MenuManager {

    private static final String[] MENU_ADMIN = {
        "Logout",
        "Tambah Buku",
        "Edit Buku",
        "Hapus Buku",
        "Lihat Semua Buku",
        "Cari Buku",
        "Tambah Anggota",
        "Lihat Semua Anggota",
        "Laporan Peminjaman"
    };

    private PerpustakaanService service;

    public MenuAdmin() {
        this.service = PerpustakaanService.getInstance();
    }

    /** Menjalankan loop menu admin */
    public void jalankan() {
        boolean running = true;
        while (running) {
            tampilkanHeader("MENU ADMIN");
            int pilihan = tampilkanMenu(MENU_ADMIN);

            try {
                switch (pilihan) {
                    case 1 -> tambahBuku();
                    case 2 -> editBuku();
                    case 3 -> hapusBuku();
                    case 4 -> lihatSemuaBuku();
                    case 5 -> cariBuku();
                    case 6 -> tambahAnggota();
                    case 7 -> lihatSemuaAnggota();
                    case 8 -> laporanPeminjaman();
                    case 0 -> {
                        service.logout();
                        cetakInfo("Logout berhasil.");
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

    private void tambahBuku() {
        tampilkanHeader("TAMBAH BUKU");

        String id = service.generateIdBuku();
        String judul = bacaInputWajib("Judul");
        int tahun = bacaIntPositif("Tahun Terbit");
        String kategori = bacaInput("Kategori");
        String penerbit = bacaInput("Penerbit");

        cetakInfo("Tipe: 1. Buku Fisik  2. Buku Digital  3. Jurnal");
        int tipe = bacaInt("Pilih tipe");

        ItemPerpustakaan item;
        switch (tipe) {
            case 1 -> {
                int halaman = bacaIntPositif("Jumlah Halaman");
                String rak = bacaInput("Lokasi Rak");
                item = new BukuFisik(id, judul, tahun, kategori, penerbit, halaman, rak);
            }
            case 2 -> {
                double ukuran = bacaDouble("Ukuran File (MB)");
                String format = bacaInput("Format (PDF/EPUB)");
                item = new BukuDigital(id, judul, tahun, kategori, penerbit, ukuran, format);
            }
            case 3 -> {
                int volume = bacaIntPositif("Volume");
                int nomor = bacaIntPositif("Nomor");
                String bidang = bacaInput("Bidang Ilmu");
                item = new Jurnal(id, judul, tahun, kategori, penerbit, volume, nomor, bidang);
            }
            default -> {
                cetakError("Tipe tidak valid.");
                return;
            }
        }

        service.tambahBuku(item);
        cetakSukses("Item '" + judul + "' berhasil ditambahkan (ID: " + id + ").");
    }

    private void editBuku() {
        tampilkanHeader("EDIT BUKU");

        String id = bacaInput("ID Buku");
        ItemPerpustakaan item = service.getBukuById(id);

        if (item == null) {
            cetakError("Item dengan ID '" + id + "' tidak ditemukan.");
            return;
        }

        cetakInfo("Data saat ini: " + item);
        cetakInfo("Kosongkan field jika tidak ingin mengubah.");

        String judul = bacaInput("Judul baru");
        if (!judul.isEmpty()) item.setJudul(judul);

        String tahunStr = bacaInput("Tahun Terbit baru");
        if (!tahunStr.isEmpty()) {
            try {
                item.setTahunTerbit(Integer.parseInt(tahunStr));
            } catch (NumberFormatException e) {
                cetakError("Tahun tidak valid, menggunakan tahun lama.");
            }
        }

        String kategori = bacaInput("Kategori baru");
        if (!kategori.isEmpty()) item.setKategori(kategori);

        String penerbit = bacaInput("Penerbit baru");
        if (!penerbit.isEmpty()) item.setPenerbit(penerbit);

        // Edit field spesifik per tipe
        if (item instanceof BukuFisik bukuFisik) {
            String halamanStr = bacaInput("Jumlah Halaman baru");
            if (!halamanStr.isEmpty()) {
                try {
                    int h = Integer.parseInt(halamanStr);
                    if (h > 0) bukuFisik.setJumlahHalaman(h);
                    else cetakError("Jumlah halaman harus positif.");
                } catch (NumberFormatException e) {
                    cetakError("Halaman tidak valid.");
                }
            }
            String rak = bacaInput("Lokasi Rak baru");
            if (!rak.isEmpty()) bukuFisik.setLokasiRak(rak);
        } else if (item instanceof BukuDigital bukuDigital) {
            String ukuranStr = bacaInput("Ukuran File baru (MB)");
            if (!ukuranStr.isEmpty()) {
                try {
                    bukuDigital.setUkuranFile(Double.parseDouble(ukuranStr));
                } catch (NumberFormatException e) {
                    cetakError("Ukuran file tidak valid.");
                }
            }
            String format = bacaInput("Format baru");
            if (!format.isEmpty()) bukuDigital.setFormat(format);
        } else if (item instanceof Jurnal jurnal) {
            String volStr = bacaInput("Volume baru");
            if (!volStr.isEmpty()) {
                try {
                    int v = Integer.parseInt(volStr);
                    if (v > 0) jurnal.setVolume(v);
                    else cetakError("Volume harus positif.");
                } catch (NumberFormatException e) {
                    cetakError("Volume tidak valid.");
                }
            }
            String nomorStr = bacaInput("Nomor baru");
            if (!nomorStr.isEmpty()) {
                try {
                    int n = Integer.parseInt(nomorStr);
                    if (n > 0) jurnal.setNomor(n);
                    else cetakError("Nomor harus positif.");
                } catch (NumberFormatException e) {
                    cetakError("Nomor tidak valid.");
                }
            }
            String bidang = bacaInput("Bidang Ilmu baru");
            if (!bidang.isEmpty()) jurnal.setBidang(bidang);
        }

        // Objek sudah diupdate in-memory via reference, cukup simpan ke file
        service.simpanSemua();
        cetakSukses("Item berhasil diupdate.");
    }

    private void hapusBuku() {
        tampilkanHeader("HAPUS BUKU");

        String id = bacaInput("ID Buku");
        ItemPerpustakaan item = service.getBukuById(id);

        if (item == null) {
            cetakError("Item dengan ID '" + id + "' tidak ditemukan.");
            return;
        }

        cetakInfo("Data: " + item);
        String konfirmasi = bacaInput("Yakin hapus? (y/n)");

        if (konfirmasi.equalsIgnoreCase("y")) {
            try {
                service.hapusBuku(id);
                cetakSukses("Item berhasil dihapus.");
            } catch (BukuTidakDitemukanException e) {
                cetakError(e.getMessage());
            }
        } else {
            cetakInfo("Penghapusan dibatalkan.");
        }
    }

    private void lihatSemuaBuku() {
        tampilkanHeader("DAFTAR SEMUA BUKU");

        List<ItemPerpustakaan> daftar = service.getAllBuku();

        if (daftar.isEmpty()) {
            cetakInfo("Belum ada item di perpustakaan.");
            return;
        }

        cetakInfo("Total: " + daftar.size() + " item\n");
        for (ItemPerpustakaan item : daftar) {
            System.out.println("  " + item);
        }
    }

    private void cariBuku() {
        super.cariBuku(service, false);
    }

    // ========== ANGGOTA ==========

    private void tambahAnggota() {
        tampilkanHeader("TAMBAH ANGGOTA");

        String id = service.generateIdAnggota();
        String nama = bacaInputWajib("Nama Lengkap");
        String email = bacaInput("Email");
        String telepon = bacaInput("Telepon");

        Anggota anggota = new Anggota(id, nama, email, telepon);
        service.tambahAnggota(anggota);
        cetakSukses("Anggota '" + nama + "' berhasil ditambahkan (ID: " + id + ").");
    }

    private void lihatSemuaAnggota() {
        tampilkanHeader("DAFTAR SEMUA ANGGOTA");

        List<Anggota> daftar = service.getAllAnggota();

        if (daftar.isEmpty()) {
            cetakInfo("Belum ada anggota terdaftar.");
            return;
        }

        cetakInfo("Total: " + daftar.size() + " anggota\n");
        for (Anggota a : daftar) {
            System.out.println("  " + a);
        }
    }

    // ========== LAPORAN ==========

    private void laporanPeminjaman() {
        tampilkanHeader("LAPORAN PEMINJAMAN");

        List<Peminjaman> peminjaman = service.getPeminjamanAktif();
        List<Peminjaman> semua = service.getAllPeminjaman();
        double totalDenda = service.getTotalDenda();

        cetakInfo("Peminjaman Aktif: " + peminjaman.size());
        cetakInfo("Total Transaksi: " + semua.size());
        cetakInfo("Total Denda: Rp " + String.format("%,.0f", totalDenda));

        if (!peminjaman.isEmpty()) {
            cetakInfo("\nDaftar Peminjaman Aktif:");
            for (Peminjaman p : peminjaman) {
                ItemPerpustakaan item = service.getBukuById(p.getIdItem());
                Anggota anggota = service.getAnggotaById(p.getIdAnggota());
                String namaItem = (item != null) ? item.getJudul() : p.getIdItem();
                String namaAnggota = (anggota != null) ? anggota.getNama() : p.getIdAnggota();
                System.out.println("  #" + p.getIdPeminjaman()
                    + " | " + namaAnggota
                    + " | " + namaItem
                    + " | Pinjam: " + p.getTanggalPinjam()
                    + " | Kembali: " + p.getTanggalKembali());
            }
        }
    }
}
