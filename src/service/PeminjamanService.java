package service;

import collection.Repository;
import exception.*;
import model.Anggota;
import model.Peminjaman;
import model.StatusPeminjaman;
import model.base.ItemPerpustakaan;

import java.time.LocalDate;
import java.util.List;

public class PeminjamanService {
    private Repository<ItemPerpustakaan> repoBuku;
    private Repository<Anggota> repoAnggota;
    private Repository<Peminjaman> repoPeminjaman;

    public PeminjamanService(Repository<ItemPerpustakaan> repoBuku,
                             Repository<Anggota> repoAnggota,
                             Repository<Peminjaman> repoPeminjaman) {
        this.repoBuku = repoBuku;
        this.repoAnggota = repoAnggota;
        this.repoPeminjaman = repoPeminjaman;
    }

    public void pinjamBuku(String idBuku, String idAnggota)
            throws BukuTidakDitemukanException, AnggotaTidakValidException,
            BukuTidakTersediaException, PeminjamanMelebihiBatasException {

        ItemPerpustakaan item = repoBuku.findById(idBuku);
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item dengan ID '" + idBuku + "' tidak ditemukan.");
        }

        Anggota anggota = repoAnggota.findById(idAnggota);
        if (anggota == null) {
            throw new AnggotaTidakValidException(
                    "Anggota dengan ID '" + idAnggota + "' tidak ditemukan.");
        }

        if (!item.isTersedia()) {
            throw new BukuTidakTersediaException(
                    "Item '" + item.getJudul() + "' sedang dipinjam orang lain.");
        }

        if (!anggota.bisaPinjam()) {
            throw new PeminjamanMelebihiBatasException(
                    "Anggota '" + anggota.getNama() + "' sudah mencapai batas pinjam ("
                            + Anggota.MAX_PINJAM + ").");
        }

        String idPeminjaman = repoPeminjaman.generateId("P");
        LocalDate tanggalPinjam = LocalDate.now();
        LocalDate tanggalKembali = tanggalPinjam.plusDays(7);
        Peminjaman peminjaman = new Peminjaman(idPeminjaman, idAnggota, idBuku,
                tanggalPinjam, tanggalKembali);

        item.pinjam();
        anggota.tambahPinjaman();

        repoPeminjaman.add(peminjaman);
    }

    public Peminjaman kembalikanBuku(String idPeminjaman)
            throws PeminjamanTidakDitemukanException, BukuTidakDitemukanException, AnggotaTidakValidException, IllegalStateException {

        Peminjaman peminjaman = repoPeminjaman.findById(idPeminjaman);
        if (peminjaman == null) {
            throw new PeminjamanTidakDitemukanException(
                    "Peminjaman dengan ID '" + idPeminjaman + "' tidak ditemukan.");
        }

        if (peminjaman.getStatus() != StatusPeminjaman.DIPINJAM) {
            throw new IllegalStateException(
                "Peminjaman #" + idPeminjaman + " sudah dikembalikan sebelumnya.");
        }

        ItemPerpustakaan item = repoBuku.findById(peminjaman.getIdItem());
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item terkait dengan ID '" + peminjaman.getIdItem() + "' tidak ditemukan.");
        }

        Anggota anggota = repoAnggota.findById(peminjaman.getIdAnggota());
        if (anggota == null) {
            throw new AnggotaTidakValidException(
                    "Anggota terkait dengan ID '" + peminjaman.getIdAnggota() + "' tidak ditemukan.");
        }

        int hariTerlambat = peminjaman.kembalikan();
        item.kembalikan();
        anggota.kurangiPinjaman();

        if (hariTerlambat > 0) {
            double denda = item.hitungDenda(hariTerlambat);
            peminjaman.setDenda(denda);
        }

        return peminjaman;
    }

    public void perpanjangPeminjaman(String idPeminjaman)
            throws PeminjamanTidakDitemukanException, IllegalStateException {
        Peminjaman peminjaman = repoPeminjaman.findById(idPeminjaman);
        if (peminjaman == null) {
            throw new PeminjamanTidakDitemukanException(
                    "Peminjaman dengan ID '" + idPeminjaman + "' tidak ditemukan.");
        }

        if (peminjaman.getStatus() != StatusPeminjaman.DIPINJAM) {
            throw new IllegalStateException(
                    "Peminjaman #" + idPeminjaman + " sudah selesai, tidak bisa diperpanjang.");
        }

        peminjaman.perpanjang();
    }

    public List<Peminjaman> getRiwayatPeminjaman(String idAnggota) {
        return repoPeminjaman.find(p -> p.getIdAnggota().equals(idAnggota));
    }

    public List<Peminjaman> getPeminjamanAktif() {
        return repoPeminjaman.find(p -> p.getStatus() == StatusPeminjaman.DIPINJAM);
    }

    public List<Peminjaman> getAllPeminjaman() {
        return repoPeminjaman.getAll();
    }

    public Peminjaman getPeminjamanById(String idPeminjaman) {
        return repoPeminjaman.findById(idPeminjaman);
    }

    public double getTotalDenda() {
        return repoPeminjaman.getAll().stream()
                .mapToDouble(Peminjaman::getDenda)
                .sum();
    }
}
