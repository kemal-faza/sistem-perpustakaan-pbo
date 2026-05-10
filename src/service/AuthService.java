package service;

import collection.Repository;
import exception.AnggotaTidakValidException;
import model.Admin;
import model.Anggota;

/**
 * Service untuk autentikasi admin dan anggota.
 * Memverifikasi kredensial login.
 */
public class AuthService {

    private Repository<Anggota> repoAnggota;

    /** Admin tunggal sistem (bawaan) */
    private Admin admin;

    /**
     * Constructor AuthService.
     * @param repoAnggota repository anggota untuk verifikasi login anggota
     */
    public AuthService(Repository<Anggota> repoAnggota) {
        this.repoAnggota = repoAnggota;
        this.admin = new Admin();
    }

    /**
     * Memvalidasi login admin.
     * @param username username admin
     * @param password password admin
     * @return true jika kredensial cocok
     */
    public boolean validateAdmin(String username, String password) {
        return admin.getUsername().equals(username)
            && admin.validatePassword(password);
    }

    /**
     * Memvalidasi login anggota berdasarkan ID.
     * @param idAnggota ID anggota
     * @return objek Anggota jika ditemukan
     * @throws AnggotaTidakValidException jika ID tidak ditemukan
     */
    public Anggota loginAnggota(String idAnggota) throws AnggotaTidakValidException {
        Anggota anggota = repoAnggota.findById(idAnggota);
        if (anggota == null) {
            throw new AnggotaTidakValidException(
                "Anggota dengan ID '" + idAnggota + "' tidak ditemukan.");
        }
        return anggota;
    }

    /** Mendapatkan referensi admin */
    public Admin getAdmin() {
        return admin;
    }
}
