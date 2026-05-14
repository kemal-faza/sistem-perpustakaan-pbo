package unit.service;

import exception.*;
import model.*;
import model.base.ItemPerpustakaan;
import org.junit.jupiter.api.*;
import service.PerpustakaanService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test for PerpustakaanService.
 * Backs up and restores real data files so tests are isolated.
 * Phase 2 will refactor the service to support dependency injection for cleaner testing.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerpustakaanServiceTest {

    private static PerpustakaanService service;
    private static final String DATA_DIR = "data";
    private static final Path BACKUP_DIR = Path.of("/tmp/opencode/pbo_backup_" + System.currentTimeMillis());

    @BeforeAll
    static void backupAndInit() throws Exception {
        // Backup existing data files
        Files.createDirectories(BACKUP_DIR);
        for (String file : List.of("buku.json", "anggota.json", "peminjaman.json")) {
            Path src = Path.of(DATA_DIR, file);
            if (Files.exists(src)) {
                Files.copy(src, BACKUP_DIR.resolve(file), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Reset singleton
        resetSingleton();

        // Get service instance (fresh with empty data)
        service = PerpustakaanService.getInstance();
    }

    @AfterAll
    static void restore() throws Exception {
        // Restore original data files
        for (String file : List.of("buku.json", "anggota.json", "peminjaman.json")) {
            Path backup = BACKUP_DIR.resolve(file);
            Path dest = Path.of(DATA_DIR, file);
            if (Files.exists(backup)) {
                Files.copy(backup, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Cleanup backup dir
        for (String file : List.of("buku.json", "anggota.json", "peminjaman.json")) {
            Files.deleteIfExists(BACKUP_DIR.resolve(file));
        }
        Files.deleteIfExists(BACKUP_DIR);

        // Reset singleton so app continues normally
        resetSingleton();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Clear data files before each test to prevent state leakage
        Files.writeString(Path.of(DATA_DIR, "buku.json"), "[]");
        Files.writeString(Path.of(DATA_DIR, "anggota.json"), "[]");
        Files.writeString(Path.of(DATA_DIR, "peminjaman.json"), "[]");
        // Reset singleton
        resetSingleton();
        service = PerpustakaanService.getInstance();
    }

    private static void resetSingleton() throws Exception {
        Field instance = PerpustakaanService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    // ========== AUTH TESTS ==========

    @Test
    @Order(1)
    void testLoginAdminSuccess() {
        assertTrue(service.loginAdmin("admin", "admin123"));
        assertTrue(service.isAdmin());
        assertFalse(service.isAnggota());
    }

    @Test
    @Order(2)
    void testLoginAdminFail() {
        assertFalse(service.loginAdmin("wrong", "pass"));
    }

    @Test
    @Order(3)
    void testLoginAnggotaSuccess() throws AnggotaTidakValidException {
        service.tambahAnggota(new Anggota("A001", "Budi"));
        assertTrue(service.loginAnggota("A001"));
        assertTrue(service.isAnggota());
        assertFalse(service.isAdmin());
        assertEquals("Budi", service.getCurrentAnggota().getNama());
    }

    @Test
    @Order(4)
    void testLoginAnggotaNotFound() {
        assertThrows(AnggotaTidakValidException.class,
                () -> service.loginAnggota("A999"));
    }

    @Test
    @Order(5)
    void testLogout() {
        service.loginAdmin("admin", "admin123");
        assertTrue(service.isAdmin());

        service.logout();
        assertFalse(service.isAdmin());
        assertFalse(service.isAnggota());
        assertNull(service.getCurrentUser());
    }

    // ========== BUKU TESTS ==========

    @Test
    @Order(10)
    void testTambahBukuBaru() {
        BukuFisik buku = new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3);

        AddResult result = service.tambahBuku(buku);
        assertEquals(AddResult.BARU, result);
        assertEquals(1, service.getAllBuku().size());
    }

    @Test
    @Order(11)
    void testTambahBukuDuplikatTambahStok() {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3));

        // Buku dengan judul + tipe + penulis yang sama
        AddResult result = service.tambahBuku(new BukuFisik("B002", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 2));

        assertEquals(AddResult.STOK, result);
        // stok early + new = 3 + 2 = 5
        ItemPerpustakaan item = service.getBukuById("B001");
        assertEquals(5, item.getStok());
    }

    @Test
    @Order(12)
    void testHapusBuku() throws BukuTidakDitemukanException {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 1));

        service.hapusBuku("B001");
        assertEquals(0, service.getAllBuku().size());
    }

    @Test
    @Order(13)
    void testHapusBukuNotFound() {
        assertThrows(BukuTidakDitemukanException.class,
                () -> service.hapusBuku("B999"));
    }

    @Test
    @Order(14)
    void testCariBukuByJudul() {
        service.tambahBuku(new BukuFisik("B001", "Pemrograman Java", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3));
        service.tambahBuku(new BukuFisik("B002", "Struktur Data", 2023,
                Kategori.TEKNOLOGI, "Andi", "Rosa",
                420, "A1-02", 2));

        List<ItemPerpustakaan> hasil = service.cariBuku("Java");
        assertEquals(1, hasil.size());
        assertEquals("Pemrograman Java", hasil.get(0).getJudul());
    }

    @Test
    @Order(15)
    void testCariBukuKosongKembalikanSemua() {
        service.tambahBuku(new BukuFisik("B001", "Buku Satu", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-01", 1));
        service.tambahBuku(new BukuFisik("B002", "Buku Dua", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 200, "A1-02", 1));

        List<ItemPerpustakaan> hasil = service.cariBuku("");
        assertEquals(2, hasil.size());
    }

    @Test
    @Order(16)
    void testCariBukuTidakAda() {
        List<ItemPerpustakaan> hasil = service.cariBuku("TidakAda");
        assertTrue(hasil.isEmpty());
    }

    @Test
    @Order(17)
    void testGetBukuById() {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3));

        assertNotNull(service.getBukuById("B001"));
        assertNull(service.getBukuById("B999"));
    }

    // ========== ANGGOTA TESTS ==========

    @Test
    @Order(20)
    void testTambahAnggota() {
        service.tambahAnggota(new Anggota("A001", "Budi"));
        assertEquals(1, service.getAllAnggota().size());
    }

    @Test
    @Order(21)
    void testGetAnggotaById() {
        service.tambahAnggota(new Anggota("A001", "Budi"));
        assertNotNull(service.getAnggotaById("A001"));
        assertEquals("Budi", service.getAnggotaById("A001").getNama());
        assertNull(service.getAnggotaById("A999"));
    }

    // ========== PEMINJAMAN TESTS ==========

    @Test
    @Order(30)
    void testPinjamBukuSukses() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 2));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        service.pinjamBuku("B001", "A001");

        // Stok berkurang
        assertEquals(1, service.getBukuById("B001").getTersedia());
        // Anggota punya pinjaman aktif
        assertEquals(1, service.getAnggotaById("A001").getPinjamanAktif());
        // Ada 1 peminjaman aktif
        assertEquals(1, service.getPeminjamanAktif().size());
    }

    @Test
    @Order(31)
    void testPinjamBukuStokHabis() {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 1));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        assertDoesNotThrow(() -> service.pinjamBuku("B001", "A001"));

        // Stok habis, pinjam lagi harus error
        assertThrows(BukuTidakTersediaException.class,
                () -> service.pinjamBuku("B001", "A001"));
    }

    @Test
    @Order(32)
    void testPinjamBukuBatasMaksimal() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "Buku Satu", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-01", 3));
        service.tambahBuku(new BukuFisik("B002", "Buku Dua", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-02", 3));
        service.tambahBuku(new BukuFisik("B003", "Buku Tiga", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-03", 3));
        service.tambahBuku(new BukuFisik("B004", "Buku Empat", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-04", 3));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        // Pinjam 3 buku (max = 3)
        service.pinjamBuku("B001", "A001");
        service.pinjamBuku("B002", "A001");
        service.pinjamBuku("B003", "A001");

        // Keempat seharusnya ditolak
        assertThrows(PeminjamanMelebihiBatasException.class,
                () -> service.pinjamBuku("B004", "A001"));
    }

    @Test
    @Order(33)
    void testPinjamBukuInvalidAnggota() {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 2));

        assertThrows(AnggotaTidakValidException.class,
                () -> service.pinjamBuku("B001", "A999"));
    }

    @Test
    @Order(34)
    void testPinjamBukuInvalidBuku() {
        service.tambahAnggota(new Anggota("A001", "Budi"));

        assertThrows(BukuTidakDitemukanException.class,
                () -> service.pinjamBuku("B999", "A001"));
    }

    @Test
    @Order(35)
    void testKembalikanBukuTepatWaktu() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 2));
        service.tambahAnggota(new Anggota("A001", "Budi"));
        service.pinjamBuku("B001", "A001");

        String idPeminjaman = service.getPeminjamanAktif().get(0).getIdPeminjaman();
        Peminjaman result = service.kembalikanBuku(idPeminjaman);

        assertEquals(StatusPeminjaman.DIKEMBALIKAN, result.getStatus());
        assertEquals(0, result.getDenda());
        assertEquals(2, service.getBukuById("B001").getTersedia()); // stok kembali
        assertEquals(0, service.getAnggotaById("A001").getPinjamanAktif());
    }

    @Test
    @Order(36)
    void testKembalikanBukuInvalidId() {
        assertThrows(PeminjamanTidakDitemukanException.class,
                () -> service.kembalikanBuku("P999"));
    }

    @Test
    @Order(37)
    void testKembalikanBukuDuplikat() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 2));
        service.tambahAnggota(new Anggota("A001", "Budi"));
        service.pinjamBuku("B001", "A001");

        String idPeminjaman = service.getPeminjamanAktif().get(0).getIdPeminjaman();
        service.kembalikanBuku(idPeminjaman);

        // Kembalikan lagi — harus error
        assertThrows(IllegalStateException.class,
                () -> service.kembalikanBuku(idPeminjaman));
    }

    @Test
    @Order(38)
    void testPerpanjangPeminjaman() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 2));
        service.tambahAnggota(new Anggota("A001", "Budi"));
        service.pinjamBuku("B001", "A001");

        String idPeminjaman = service.getPeminjamanAktif().get(0).getIdPeminjaman();
        service.perpanjangPeminjaman(idPeminjaman);

        // Setelah perpanjang masih aktif
        assertEquals(StatusPeminjaman.DIPINJAM,
                service.getPeminjamanById(idPeminjaman).getStatus());
    }

    @Test
    @Order(39)
    void testPerpanjangPeminjamanInvalidId() {
        assertThrows(PeminjamanTidakDitemukanException.class,
                () -> service.perpanjangPeminjaman("P999"));
    }

    @Test
    @Order(40)
    void testGetRiwayatPeminjaman() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 5));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        service.pinjamBuku("B001", "A001");
        String idP1 = service.getPeminjamanAktif().get(0).getIdPeminjaman();
        service.kembalikanBuku(idP1);

        service.pinjamBuku("B001", "A001");

        List<Peminjaman> riwayat = service.getRiwayatPeminjaman("A001");
        assertEquals(2, riwayat.size());
    }

    @Test
    @Order(41)
    void testGetAllPeminjaman() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 5));
        service.tambahAnggota(new Anggota("A001", "Budi"));
        service.tambahAnggota(new Anggota("A002", "Siti"));

        service.pinjamBuku("B001", "A001");
        service.pinjamBuku("B001", "A002");

        assertEquals(2, service.getAllPeminjaman().size());
    }

    @Test
    @Order(42)
    void testGetPeminjamanAktif() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 5));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        service.pinjamBuku("B001", "A001");
        assertEquals(1, service.getPeminjamanAktif().size());

        String idP = service.getPeminjamanAktif().get(0).getIdPeminjaman();
        service.kembalikanBuku(idP);
        assertEquals(0, service.getPeminjamanAktif().size());
    }

    @Test
    @Order(43)
    void testGetPeminjamanById() throws Exception {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 5));
        service.tambahAnggota(new Anggota("A001", "Budi"));
        service.pinjamBuku("B001", "A001");

        String idP = service.getPeminjamanAktif().get(0).getIdPeminjaman();
        assertNotNull(service.getPeminjamanById(idP));
        assertNull(service.getPeminjamanById("P999"));
    }

    @Test
    @Order(44)
    void testGetTotalDenda() throws Exception {
        // Untuk test ini kita hanya bisa cek bahwa method berjalan
        // Denda aktual tergantung tanggal
        assertEquals(0.0, service.getTotalDenda());
    }

    // ========== ID GENERATION ==========

    @Test
    @Order(50)
    void testGenerateIdBuku() {
        String id1 = service.generateIdBuku();
        assertTrue(id1.startsWith("B"));
        assertEquals(4, id1.length());
    }

    @Test
    @Order(51)
    void testGenerateIdAnggota() {
        String id1 = service.generateIdAnggota();
        assertTrue(id1.startsWith("A"));
        assertEquals(4, id1.length());
    }

    @Test
    @Order(52)
    void testGenerateIdIncrement() {
        String id1 = service.generateIdBuku();
        service.tambahBuku(new BukuFisik("B001", "Test", 2024,
                Kategori.UMUM, "Pub", "Penulis", 100, "A1-01", 1));

        String id2 = service.generateIdBuku();
        assertNotEquals(id1, id2);
    }

    // ========== PERSISTENCE ==========

    @Test
    @Order(60)
    void testSimpanSemua() {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        // Method should not throw
        assertDoesNotThrow(() -> service.simpanSemua());
    }

    @Test
    @Order(61)
    void testLoadSampleData() {
        // Data already empty from setUp, should load samples
        boolean loaded = service.loadSampleData();
        assertTrue(loaded);
        assertTrue(service.getAllBuku().size() > 0);
    }

    @Test
    @Order(62)
    void testLoadSampleDataOnlyOnce() {
        service.loadSampleData();
        boolean loadedAgain = service.loadSampleData();
        assertFalse(loadedAgain); // sampleLoaded flag = true
    }
}
