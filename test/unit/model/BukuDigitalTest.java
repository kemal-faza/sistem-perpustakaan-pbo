package unit.model;

import model.BukuDigital;
import model.Kategori;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BukuDigitalTest {

    @Test
    void testConstructorAndGetters() {
        BukuDigital buku = new BukuDigital("B003", "Belajar Java", 2024,
                Kategori.TEKNOLOGI, "E-Book Publisher", "Budi Raharjo",
                5.2, "PDF");

        assertEquals("B003", buku.getId());
        assertEquals("Belajar Java", buku.getJudul());
        assertEquals(5.2, buku.getUkuranFile(), 0.001);
        assertEquals("PDF", buku.getFormat());
        assertEquals(1, buku.getStok()); // digital selalu stok=1
        assertEquals("Buku Digital", buku.getTipe());
    }

    @Test
    void testHitungDenda() {
        BukuDigital buku = new BukuDigital();
        assertEquals(2500, buku.hitungDenda(5)); // 5 * 500
        assertEquals(0, buku.hitungDenda(0));
    }

    @Test
    void testPinjamKembalikan() {
        BukuDigital buku = new BukuDigital("B003", "Java", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis",
                5.2, "PDF");
        assertTrue(buku.isTersedia());

        buku.pinjam();
        assertFalse(buku.isTersedia());

        buku.kembalikan();
        assertTrue(buku.isTersedia());
    }

    @Test
    void testToString() {
        BukuDigital buku = new BukuDigital("B003", "Java Ebook", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis",
                5.2, "PDF");
        String str = buku.toString();
        assertTrue(str.contains("Java Ebook"));
        assertTrue(str.contains("5.2"));
        assertTrue(str.contains("PDF"));
    }

    @Test
    void testSetter() {
        BukuDigital buku = new BukuDigital();
        buku.setUkuranFile(10.5);
        buku.setFormat("EPUB");
        assertEquals(10.5, buku.getUkuranFile(), 0.001);
        assertEquals("EPUB", buku.getFormat());
    }
}
