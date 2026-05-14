package unit.model;

import model.BukuFisik;
import model.Kategori;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BukuFisikTest {

    @Test
    void testConstructorAndGetters() {
        BukuFisik buku = new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3);

        assertEquals("B001", buku.getId());
        assertEquals("PBO", buku.getJudul());
        assertEquals(2024, buku.getTahunTerbit());
        assertEquals(Kategori.TEKNOLOGI, buku.getKategori());
        assertEquals("Informatika", buku.getPenerbit());
        assertEquals("Tim PBO", buku.getPenulis());
        assertEquals(350, buku.getJumlahHalaman());
        assertEquals("A1-01", buku.getLokasiRak());
        assertEquals(3, buku.getStok());
        assertEquals("Buku Fisik", buku.getTipe());
    }

    @Test
    void testConstructorDefault() {
        BukuFisik buku = new BukuFisik();
        assertNull(buku.getId());
        assertEquals(1, buku.getStok());
        assertEquals(1, buku.getTersedia());
    }

    @Test
    void testHitungDenda() {
        BukuFisik buku = new BukuFisik();
        assertEquals(5000, buku.hitungDenda(5)); // 5 * 1000
        assertEquals(0, buku.hitungDenda(0));
    }

    @Test
    void testKetersediaan() {
        BukuFisik buku = new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3);

        assertTrue(buku.isTersedia());
        assertEquals(3, buku.getTersedia());

        buku.pinjam();
        assertTrue(buku.isTersedia());
        assertEquals(2, buku.getTersedia());

        buku.pinjam();
        buku.pinjam();
        assertFalse(buku.isTersedia());
        assertEquals(0, buku.getTersedia());
    }

    @Test
    void testPinjamDanKembalikan() {
        BukuFisik buku = new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 1);

        assertEquals(1, buku.getTersedia());
        buku.pinjam();
        assertEquals(0, buku.getTersedia());
        assertFalse(buku.isTersedia());

        buku.kembalikan();
        assertEquals(1, buku.getTersedia());
        assertTrue(buku.isTersedia());
    }

    @Test
    void testPinjamStokHabisTidak() {
        BukuFisik buku = new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 1);
        buku.pinjam();
        assertThrows(IllegalStateException.class, () -> buku.pinjam());
    }

    @Test
    void testToString() {
        BukuFisik buku = new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 3);
        String str = buku.toString();
        assertTrue(str.contains("PBO"));
        assertTrue(str.contains("A1-01"));
        assertTrue(str.contains("350"));
    }

    @Test
    void testSetter() {
        BukuFisik buku = new BukuFisik();
        buku.setJumlahHalaman(500);
        buku.setLokasiRak("B2-10");

        assertEquals(500, buku.getJumlahHalaman());
        assertEquals("B2-10", buku.getLokasiRak());
    }
}
