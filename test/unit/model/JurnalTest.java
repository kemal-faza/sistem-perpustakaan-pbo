package unit.model;

import model.Jurnal;
import model.Kategori;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JurnalTest {

    @Test
    void testConstructorAndGetters() {
        Jurnal jurnal = new Jurnal("J001", "Jurnal Ilmu Komputer", 2024,
                Kategori.ILMIAH, "Universitas", "Tim Peneliti",
                12, 1, "Ilmu Komputer", 2);

        assertEquals("J001", jurnal.getId());
        assertEquals("Jurnal Ilmu Komputer", jurnal.getJudul());
        assertEquals(12, jurnal.getVolume());
        assertEquals(1, jurnal.getNomor());
        assertEquals("Ilmu Komputer", jurnal.getBidang());
        assertEquals(2, jurnal.getStok());
        assertEquals("Jurnal", jurnal.getTipe());
    }

    @Test
    void testHitungDenda() {
        Jurnal jurnal = new Jurnal();
        assertEquals(10000, jurnal.hitungDenda(5)); // 5 * 2000
        assertEquals(0, jurnal.hitungDenda(0));
    }

    @Test
    void testToString() {
        Jurnal jurnal = new Jurnal("J001", "Jurnal AI", 2024,
                Kategori.ILMIAH, "Univ", "Peneliti",
                5, 2, "AI", 1);
        String str = jurnal.toString();
        assertTrue(str.contains("Jurnal AI"));
        assertTrue(str.contains("Vol.5"));
        assertTrue(str.contains("No.2"));
        assertTrue(str.contains("AI"));
    }

    @Test
    void testSetter() {
        Jurnal jurnal = new Jurnal();
        jurnal.setVolume(15);
        jurnal.setNomor(3);
        jurnal.setBidang("Biologi");
        assertEquals(15, jurnal.getVolume());
        assertEquals(3, jurnal.getNomor());
        assertEquals("Biologi", jurnal.getBidang());
    }

    @Test
    void testPinjam() {
        Jurnal jurnal = new Jurnal("J001", "Jurnal", 2024,
                Kategori.ILMIAH, "Univ", "Peneliti",
                1, 1, "Fisika", 2);

        assertTrue(jurnal.isTersedia());
        jurnal.pinjam();
        jurnal.pinjam();
        assertFalse(jurnal.isTersedia());

        jurnal.kembalikan();
        assertTrue(jurnal.isTersedia());
    }
}
