package service;

import com.google.gson.reflect.TypeToken;
import collection.Repository;
import exception.*;
import interfaces.ISearchable;
import model.*;
import model.base.ItemPerpustakaan;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service utama sistem perpustakaan.
 * Menggunakan pattern Singleton. Mengelola semua business logic
 * dan mengorkestrasi interaksi antara model-model.
 */
public class PerpustakaanService {

    /** Path file JSON untuk persistensi */
    private static final String FILE_BUKU = "data/buku.json";
    private static final String FILE_ANGGOTA = "data/anggota.json";
    private static final String FILE_PEMINJAMAN = "data/peminjaman.json";

    // === Singleton Instance ===
    private static PerpustakaanService instance;

    // === Repositories ===
    private Repository<ItemPerpustakaan> repoBuku;
    private Repository<Anggota> repoAnggota;
    private Repository<Peminjaman> repoPeminjaman;

    // === Services ===
    private AuthService authService;
    private Object currentUser;
    private Anggota currentAnggota;

    // === Data Sample Flag ===
    private volatile boolean sampleLoaded = false;

    /**
     * Constructor private (Singleton).
     * Menginisialisasi semua repository dengan TypeToken Gson.
     */
    @SuppressWarnings("unchecked")
    private PerpustakaanService() {
        // TypeToken untuk generic List (Gson membutuhkan ini karena type erasure)
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
        this.currentUser = null;
        this.currentAnggota = null;
    }

    /**
     * Mendapatkan instance tunggal PerpustakaanService (Singleton).
     */
    public static synchronized PerpustakaanService getInstance() {
        if (instance == null) {
            instance = new PerpustakaanService();
        }
        return instance;
    }

    // ========== AUTENTIKASI ==========

    /**
     * Login sebagai admin.
     * 
     * @param username username admin
     * @param password password admin
     * @return true jika login berhasil
     */
    public boolean loginAdmin(String username, String password) {
        if (authService.validateAdmin(username, password)) {
            currentUser = authService.getAdmin();
            currentAnggota = null;
            return true;
        }
        return false;
    }

    /**
     * Login sebagai anggota.
     * 
     * @param idAnggota ID anggota
     * @return true jika login berhasil
     * @throws AnggotaTidakValidException jika ID tidak ditemukan
     */
    public boolean loginAnggota(String idAnggota) throws AnggotaTidakValidException {
        Anggota anggota = authService.loginAnggota(idAnggota);
        currentUser = anggota;
        currentAnggota = anggota;
        return true;
    }

    /** Logout user saat ini */
    public void logout() {
        currentUser = null;
        currentAnggota = null;
    }

    /**
     * Mendapatkan user yang sedang login.
     * 
     * @return Admin atau Anggota, null jika belum login
     */
    public Object getCurrentUser() {
        return currentUser;
    }

    /**
     * Memeriksa apakah user saat ini adalah admin.
     */
    public boolean isAdmin() {
        return currentUser instanceof Admin;
    }

    /**
     * Memeriksa apakah user saat ini adalah anggota.
     */
    public boolean isAnggota() {
        return currentUser instanceof Anggota;
    }

    /**
     * Mendapatkan anggota yang sedang login.
     * 
     * @return Anggota saat ini, null jika admin atau belum login
     */
    public Anggota getCurrentAnggota() {
        return currentAnggota;
    }

    // ========== MANAJEMEN BUKU ==========

    /**
     * Menambahkan item baru atau menambah stok item yang sudah ada.
     * Jika judul + tipe sudah ada, stok ditambahkan. Jika belum, item baru dibuat.
     *
     * @param item item yang akan ditambahkan
     * @return "baru" jika item baru, "stok" jika hanya menambah stok item existing
     */
    public String tambahBuku(ItemPerpustakaan item) {
        // Cek duplikasi: apakah judul + tipe + penulis sudah ada?
        String itemPenulis = (item.getPenulis() != null) ? item.getPenulis() : "";
        for (ItemPerpustakaan existing : repoBuku.getAll()) {
            String existPenulis = (existing.getPenulis() != null) ? existing.getPenulis() : "";
            if (existing.getJudul().equalsIgnoreCase(item.getJudul())
                    && existing.getTipe().equals(item.getTipe())
                    && existPenulis.equalsIgnoreCase(itemPenulis)) {
                // Duplikat → tambah stok item existing
                existing.setStok(existing.getStok() + item.getStok());
                repoBuku.saveToJson();
                return "stok";
            }
        }
        // Beneran baru → add
        repoBuku.add(item);
        repoBuku.saveToJson();
        return "baru";
    }

    /**
     * Menghapus item dari perpustakaan berdasarkan ID.
     * 
     * @param idBuku ID item yang akan dihapus
     * @throws BukuTidakDitemukanException jika ID tidak ditemukan
     */
    public void hapusBuku(String idBuku) throws BukuTidakDitemukanException {
        ItemPerpustakaan item = repoBuku.findById(idBuku);
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item dengan ID '" + idBuku + "' tidak ditemukan.");
        }
        repoBuku.delete(idBuku);
        repoBuku.saveToJson();
    }

    /**
     * Mencari item berdasarkan keyword.
     * Menggunakan method cocok() dari ISearchable.
     * 
     * @param keyword kata kunci pencarian
     * @return list item yang cocok
     */
    public List<ItemPerpustakaan> cariBuku(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repoBuku.getAll();
        }

        List<ItemPerpustakaan> allBooks = repoBuku.getAll();
        List<ItemPerpustakaan> result = new ArrayList<>();

        for (ItemPerpustakaan item : allBooks) {
            if (item instanceof ISearchable && ((ISearchable) item).cocok(keyword)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Mendapatkan semua item di perpustakaan.
     */
    public List<ItemPerpustakaan> getAllBuku() {
        return repoBuku.getAll();
    }

    /**
     * Mendapatkan item berdasarkan ID.
     * 
     * @param idBuku ID item
     * @return item jika ditemukan, null jika tidak
     */
    public ItemPerpustakaan getBukuById(String idBuku) {
        return repoBuku.findById(idBuku);
    }

    // ========== MANAJEMEN ANGGOTA ==========

    /**
     * Mendaftarkan anggota baru.
     * 
     * @param anggota objek anggota yang akan ditambahkan
     */
    public void tambahAnggota(Anggota anggota) {
        repoAnggota.add(anggota);
        repoAnggota.saveToJson();
    }

    /**
     * Mendapatkan semua anggota.
     */
    public List<Anggota> getAllAnggota() {
        return repoAnggota.getAll();
    }

    /**
     * Mencari anggota berdasarkan ID.
     * 
     * @param idAnggota ID anggota
     * @return anggota jika ditemukan, null jika tidak
     */
    public Anggota getAnggotaById(String idAnggota) {
        return repoAnggota.findById(idAnggota);
    }

    // ========== PEMINJAMAN ==========

    /**
     * Memproses peminjaman item oleh anggota.
     *
     * @param idBuku    ID item yang akan dipinjam
     * @param idAnggota ID anggota yang meminjam
     * @throws BukuTidakDitemukanException      jika item tidak ditemukan
     * @throws AnggotaTidakValidException       jika anggota tidak ditemukan
     * @throws BukuTidakTersediaException       jika item sedang dipinjam
     * @throws PeminjamanMelebihiBatasException jika anggota sudah max pinjam
     */
    public void pinjamBuku(String idBuku, String idAnggota)
            throws BukuTidakDitemukanException, AnggotaTidakValidException,
            BukuTidakTersediaException, PeminjamanMelebihiBatasException {

        // Validasi: cari item
        ItemPerpustakaan item = repoBuku.findById(idBuku);
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item dengan ID '" + idBuku + "' tidak ditemukan.");
        }

        // Validasi: cari anggota
        Anggota anggota = repoAnggota.findById(idAnggota);
        if (anggota == null) {
            throw new AnggotaTidakValidException(
                    "Anggota dengan ID '" + idAnggota + "' tidak ditemukan.");
        }

        // Validasi: ketersediaan item
        if (!item.isTersedia()) {
            throw new BukuTidakTersediaException(
                    "Item '" + item.getJudul() + "' sedang dipinjam orang lain.");
        }

        // Validasi: batas pinjam anggota
        if (!anggota.bisaPinjam()) {
            throw new PeminjamanMelebihiBatasException(
                    "Anggota '" + anggota.getNama() + "' sudah mencapai batas pinjam ("
                            + Anggota.MAX_PINJAM + ").");
        }

        // Generate ID peminjaman
        String idPeminjaman = repoPeminjaman.generateId("P");

        // Buat peminjaman baru (7 hari masa pinjam)
        LocalDate tanggalPinjam = LocalDate.now();
        LocalDate tanggalKembali = tanggalPinjam.plusDays(7);
        Peminjaman peminjaman = new Peminjaman(idPeminjaman, idAnggota, idBuku,
                tanggalPinjam, tanggalKembali);

        // Update state objek terkait
        item.pinjam(); // set tersedia = false
        anggota.tambahPinjaman(); // increment pinjamanAktif

        // Simpan
        repoPeminjaman.add(peminjaman);
        repoBuku.saveToJson();
        repoAnggota.saveToJson();
        repoPeminjaman.saveToJson();
    }

    /**
     * Memproses pengembalian item.
     *
     * @param idPeminjaman ID transaksi peminjaman
     * @return objek Peminjaman yang sudah diupdate (dengan status dan denda)
     * @throws BukuTidakDitemukanException jika item terkait tidak ditemukan
     * @throws AnggotaTidakValidException  jika anggota terkait tidak ditemukan
     * @throws IllegalStateException       jika peminjaman sudah dikembalikan
     */
    public Peminjaman kembalikanBuku(String idPeminjaman)
            throws BukuTidakDitemukanException, AnggotaTidakValidException, IllegalStateException {

        // Cari peminjaman
        Peminjaman peminjaman = repoPeminjaman.findById(idPeminjaman);
        if (peminjaman == null) {
            throw new BukuTidakDitemukanException(
                    "Peminjaman dengan ID '" + idPeminjaman + "' tidak ditemukan.");
        }

        // Idempotency guard: cek apakah peminjaman sudah selesai
        if (peminjaman.getStatus() != StatusPeminjaman.DIPINJAM) {
            throw new IllegalStateException(
                "Peminjaman #" + idPeminjaman + " sudah dikembalikan sebelumnya.");
        }

        // Cari item terkait
        ItemPerpustakaan item = repoBuku.findById(peminjaman.getIdItem());
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item terkait dengan ID '" + peminjaman.getIdItem() + "' tidak ditemukan.");
        }

        // Cari anggota terkait
        Anggota anggota = repoAnggota.findById(peminjaman.getIdAnggota());
        if (anggota == null) {
            throw new AnggotaTidakValidException(
                    "Anggota terkait dengan ID '" + peminjaman.getIdAnggota() + "' tidak ditemukan.");
        }

        // Proses pengembalian di model Peminjaman — return hariTerlambat
        int hariTerlambat = peminjaman.kembalikan();

        // Kembalikan item (set tersedia = true)
        item.kembalikan();

        // Kurangi pinjaman aktif anggota
        anggota.kurangiPinjaman();

        // Hitung denda
        if (hariTerlambat > 0) {
            double denda = item.hitungDenda(hariTerlambat);
            peminjaman.setDenda(denda);
        }

        // Simpan semua perubahan
        repoPeminjaman.saveToJson();
        repoBuku.saveToJson();
        repoAnggota.saveToJson();

        return peminjaman;
    }

    /**
     * Memperpanjang masa peminjaman (3 hari).
     *
     * @param idPeminjaman ID peminjaman yang akan diperpanjang
     * @throws BukuTidakDitemukanException jika ID peminjaman tidak ditemukan
     * @throws IllegalStateException       jika peminjaman sudah selesai
     */
    public void perpanjangPeminjaman(String idPeminjaman)
            throws BukuTidakDitemukanException, IllegalStateException {
        Peminjaman peminjaman = repoPeminjaman.findById(idPeminjaman);
        if (peminjaman == null) {
            throw new BukuTidakDitemukanException(
                    "Peminjaman dengan ID '" + idPeminjaman + "' tidak ditemukan.");
        }

        if (peminjaman.getStatus() != StatusPeminjaman.DIPINJAM) {
            throw new IllegalStateException(
                    "Peminjaman #" + idPeminjaman + " sudah selesai, tidak bisa diperpanjang.");
        }

        peminjaman.perpanjang();
        repoPeminjaman.saveToJson();
    }

    /**
     * Mendapatkan riwayat peminjaman seorang anggota.
     * 
     * @param idAnggota ID anggota
     * @return list peminjaman yang dilakukan anggota tersebut
     */
    public List<Peminjaman> getRiwayatPeminjaman(String idAnggota) {
        return repoPeminjaman.find(p -> p.getIdAnggota().equals(idAnggota));
    }

    /**
     * Mendapatkan semua peminjaman yang sedang aktif (DIPINJAM).
     */
    public List<Peminjaman> getPeminjamanAktif() {
        return repoPeminjaman.find(p -> p.getStatus() == StatusPeminjaman.DIPINJAM);
    }

    /**
     * Mendapatkan semua peminjaman.
     */
    public List<Peminjaman> getAllPeminjaman() {
        return repoPeminjaman.getAll();
    }

    /**
     * Mendapatkan peminjaman berdasarkan ID.
     * @param idPeminjaman ID peminjaman
     * @return peminjaman jika ditemukan, null jika tidak
     */
    public Peminjaman getPeminjamanById(String idPeminjaman) {
        return repoPeminjaman.findById(idPeminjaman);
    }

    /**
     * Mendapatkan total pendapatan denda.
     */
    public double getTotalDenda() {
        return repoPeminjaman.getAll().stream()
                .mapToDouble(Peminjaman::getDenda)
                .sum();
    }

    // ========== PERSISTENCE ==========

    /** Menyimpan semua repository ke JSON */
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

    /**
     * Generate ID baru untuk item perpustakaan.
     */
    public String generateIdBuku() {
        return repoBuku.generateId("B");
    }

    /**
     * Generate ID baru untuk anggota.
     */
    public String generateIdAnggota() {
        return repoAnggota.generateId("A");
    }

    // ========== DATA SAMPLE ==========

    /**
     * Mengisi data sample jika repository masih kosong.
     * @return true jika data sample baru berhasil ditambahkan, false jika sudah ada data
     */
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
