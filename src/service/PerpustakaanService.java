package service;

import com.google.gson.reflect.TypeToken;
import collection.Repository;
import exception.*;
import interfaces.ILibraryService;
import model.*;
import model.base.ItemPerpustakaan;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PerpustakaanService implements ILibraryService {

    private static final String FILE_BUKU = "data/buku.json";
    private static final String FILE_ANGGOTA = "data/anggota.json";
    private static final String FILE_PEMINJAMAN = "data/peminjaman.json";

    private static PerpustakaanService instance;

    private Repository<ItemPerpustakaan> repoBuku;
    private Repository<Anggota> repoAnggota;
    private Repository<Peminjaman> repoPeminjaman;

    private AuthService authService;
    private BukuService bukuService;
    private PeminjamanService peminjamanService;

    private Admin currentAdmin;
    private Anggota currentAnggota;

    private volatile boolean sampleLoaded = false;

    @SuppressWarnings("unchecked")
    private PerpustakaanService() {
        Type bukuType = new TypeToken<ArrayList<ItemPerpustakaan>>() {
        }.getType();
        Type anggotaType = new TypeToken<ArrayList<Anggota>>() {
        }.getType();
        Type peminjamanType = new TypeToken<ArrayList<Peminjaman>>() {
        }.getType();

        this.repoBuku = new Repository<>(FILE_BUKU, bukuType);
        this.repoAnggota = new Repository<>(FILE_ANGGOTA, anggotaType);
        this.repoPeminjaman = new Repository<>(FILE_PEMINJAMAN, peminjamanType);

        this.authService = new AuthService(repoAnggota);
        this.bukuService = new BukuService(repoBuku, repoAnggota);
        this.peminjamanService = new PeminjamanService(repoBuku, repoAnggota, repoPeminjaman);

        this.currentAdmin = null;
        this.currentAnggota = null;
    }

    public static synchronized PerpustakaanService getInstance() {
        if (instance == null) {
            instance = new PerpustakaanService();
        }
        return instance;
    }

    // ========== AUTENTIKASI ==========

    public boolean loginAdmin(String username, String password) {
        if (authService.validateAdmin(username, password)) {
            currentAdmin = authService.getAdmin();
            currentAnggota = null;
            return true;
        }
        return false;
    }

    public boolean loginAnggota(String idAnggota) throws AnggotaTidakValidException {
        Anggota anggota = authService.loginAnggota(idAnggota);
        currentAdmin = null;
        currentAnggota = anggota;
        return true;
    }

    public void logout() {
        currentAdmin = null;
        currentAnggota = null;
    }

    public Object getCurrentUser() {
        return currentAdmin != null ? currentAdmin : currentAnggota;
    }

    public boolean isAdmin() {
        return currentAdmin != null;
    }

    public boolean isAnggota() {
        return currentAnggota != null;
    }

    public Anggota getCurrentAnggota() {
        return currentAnggota;
    }

    // ========== MANAJEMEN BUKU ==========

    public AddResult tambahBuku(ItemPerpustakaan item) {
        AddResult result = bukuService.tambahBuku(item);
        repoBuku.saveToJson();
        return result;
    }

    public void hapusBuku(String idBuku) throws BukuTidakDitemukanException {
        bukuService.hapusBuku(idBuku);
        repoBuku.saveToJson();
    }

    public List<ItemPerpustakaan> cariBuku(String keyword) {
        return bukuService.cariBuku(keyword);
    }

    public List<ItemPerpustakaan> getAllBuku() {
        return bukuService.getAllBuku();
    }

    public ItemPerpustakaan getBukuById(String idBuku) {
        return bukuService.getBukuById(idBuku);
    }

    // ========== MANAJEMEN ANGGOTA ==========

    public void tambahAnggota(Anggota anggota) {
        bukuService.tambahAnggota(anggota);
        repoAnggota.saveToJson();
    }

    public List<Anggota> getAllAnggota() {
        return bukuService.getAllAnggota();
    }

    public Anggota getAnggotaById(String idAnggota) {
        return bukuService.getAnggotaById(idAnggota);
    }

    // ========== PEMINJAMAN ==========

    public void pinjamBuku(String idBuku, String idAnggota)
            throws BukuTidakDitemukanException, AnggotaTidakValidException,
            BukuTidakTersediaException, PeminjamanMelebihiBatasException {
        peminjamanService.pinjamBuku(idBuku, idAnggota);
        repoBuku.saveToJson();
        repoAnggota.saveToJson();
        repoPeminjaman.saveToJson();
    }

    public Peminjaman kembalikanBuku(String idPeminjaman)
            throws PeminjamanTidakDitemukanException, BukuTidakDitemukanException, AnggotaTidakValidException, IllegalStateException {
        Peminjaman result = peminjamanService.kembalikanBuku(idPeminjaman);
        repoPeminjaman.saveToJson();
        repoBuku.saveToJson();
        repoAnggota.saveToJson();
        return result;
    }

    public void perpanjangPeminjaman(String idPeminjaman)
            throws PeminjamanTidakDitemukanException, IllegalStateException {
        peminjamanService.perpanjangPeminjaman(idPeminjaman);
        repoPeminjaman.saveToJson();
    }

    public List<Peminjaman> getRiwayatPeminjaman(String idAnggota) {
        return peminjamanService.getRiwayatPeminjaman(idAnggota);
    }

    public List<Peminjaman> getPeminjamanAktif() {
        return peminjamanService.getPeminjamanAktif();
    }

    public List<Peminjaman> getAllPeminjaman() {
        return peminjamanService.getAllPeminjaman();
    }

    public Peminjaman getPeminjamanById(String idPeminjaman) {
        return peminjamanService.getPeminjamanById(idPeminjaman);
    }

    public double getTotalDenda() {
        return peminjamanService.getTotalDenda();
    }

    // ========== PERSISTENCE ==========

    public void simpanSemua() {
        boolean ok = true;
        if (!repoBuku.saveToJson()) ok = false;
        if (!repoAnggota.saveToJson()) ok = false;
        if (!repoPeminjaman.saveToJson()) ok = false;
        if (!ok) {
            System.err.println("Peringatan: Gagal menyimpan beberapa data!");
        }
    }

    // ========== GENERATE ID ==========

    public String generateIdBuku() {
        return bukuService.generateIdBuku();
    }

    public String generateIdAnggota() {
        return bukuService.generateIdAnggota();
    }

    // ========== DATA SAMPLE ==========

    public boolean loadSampleData() {
        if (sampleLoaded) return false;

        boolean dataDitambahkan = false;

        if (repoBuku.size() == 0) {
            repoBuku.add(new BukuFisik("B001", "Pemrograman Berorientasi Objek", 2024,
                    Kategori.TEKNOLOGI, "Informatika", "Tim Pengajar PBO",
                    350, "A1-01", 3));
            repoBuku.add(new BukuFisik("B002", "Struktur Data dan Algoritma", 2023,
                    Kategori.TEKNOLOGI, "Andi Publisher", "Rosa A.S.",
                    420, "A1-02", 2));
            repoBuku.add(new BukuDigital("B003", "Belajar Java dalam Sehari", 2024,
                    Kategori.TEKNOLOGI, "E-Book Publisher", "Budi Raharjo",
                    5.2, "PDF"));
            repoBuku.add(new Jurnal("B004", "Jurnal Informatika Undip", 2024,
                    Kategori.ILMIAH, "Universitas Diponegoro", "Tim Editor JIF",
                    12, 1, "Ilmu Komputer", 2));
            repoBuku.saveToJson();
            dataDitambahkan = true;
        }

        if (repoAnggota.size() == 0) {
            repoAnggota.add(new Anggota("A001", "Budi Santoso"));
            repoAnggota.add(new Anggota("A002", "Siti Rahayu"));
            repoAnggota.add(new Anggota("A003", "Ahmad Fauzi"));
            repoAnggota.saveToJson();
            dataDitambahkan = true;
        }

        sampleLoaded = true;

        return dataDitambahkan;
    }
}
