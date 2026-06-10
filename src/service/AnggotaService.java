package service;

import collection.Repository;
import model.Anggota;

import java.util.Comparator;
import java.util.List;

/**
 * Service untuk mengelola operasi CRUD anggota perpustakaan.
 * Dipisahkan dari BukuService agar setiap service memiliki
 * tanggung jawab yang jelas (Single Responsibility Principle).
 */
public class AnggotaService {

    private Repository<Anggota> repoAnggota;

    public AnggotaService(Repository<Anggota> repoAnggota) {
        this.repoAnggota = repoAnggota;
    }

    /** Menambahkan anggota baru ke repository */
    public void tambahAnggota(Anggota anggota) {
        repoAnggota.add(anggota);
    }

    /** Mendapatkan semua anggota terdaftar (terurut by ID) */
    public List<Anggota> getAllAnggota() {
        return repoAnggota.getSorted(
            Comparator.comparing(Anggota::getId,
                Comparator.nullsLast(Comparator.naturalOrder()))
        );
    }

    /** Mendapatkan anggota berdasarkan ID (binary search) */
    public Anggota getAnggotaById(String idAnggota) {
        return repoAnggota.findByIdBinary(idAnggota);
    }

    /** Generate ID unik untuk anggota baru (format: AXXX) */
    public String generateIdAnggota() {
        return repoAnggota.generateId("A");
    }
}
