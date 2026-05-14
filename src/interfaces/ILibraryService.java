package interfaces;

import exception.*;
import model.*;
import model.base.ItemPerpustakaan;

import java.util.List;

/**
 * Interface untuk service utama perpustakaan.
 * Menerapkan Dependency Inversion — UI bergantung pada interface,
 * bukan pada concrete class.
 */
public interface ILibraryService {

    // ========== AUTENTIKASI ==========
    boolean loginAdmin(String username, String password);
    boolean loginAnggota(String idAnggota) throws AnggotaTidakValidException;
    void logout();
    Object getCurrentUser();
    boolean isAdmin();
    boolean isAnggota();
    Anggota getCurrentAnggota();

    // ========== MANAJEMEN BUKU ==========
    AddResult tambahBuku(ItemPerpustakaan item);
    void hapusBuku(String idBuku) throws BukuTidakDitemukanException;
    List<ItemPerpustakaan> cariBuku(String keyword);
    List<ItemPerpustakaan> getAllBuku();
    ItemPerpustakaan getBukuById(String idBuku);

    // ========== MANAJEMEN ANGGOTA ==========
    void tambahAnggota(Anggota anggota);
    List<Anggota> getAllAnggota();
    Anggota getAnggotaById(String idAnggota);

    // ========== PEMINJAMAN ==========
    void pinjamBuku(String idBuku, String idAnggota)
        throws BukuTidakDitemukanException, AnggotaTidakValidException,
               BukuTidakTersediaException, PeminjamanMelebihiBatasException;
    Peminjaman kembalikanBuku(String idPeminjaman)
        throws PeminjamanTidakDitemukanException, BukuTidakDitemukanException, AnggotaTidakValidException, IllegalStateException;
    void perpanjangPeminjaman(String idPeminjaman)
        throws PeminjamanTidakDitemukanException, IllegalStateException;
    List<Peminjaman> getRiwayatPeminjaman(String idAnggota);
    List<Peminjaman> getPeminjamanAktif();
    List<Peminjaman> getAllPeminjaman();
    Peminjaman getPeminjamanById(String idPeminjaman);
    double getTotalDenda();

    // ========== GENERATE ID ==========
    String generateIdBuku();
    String generateIdAnggota();

    // ========== PERSISTENCE ==========
    void simpanSemua();
    boolean loadSampleData();
}
