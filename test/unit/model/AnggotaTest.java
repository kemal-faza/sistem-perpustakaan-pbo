package unit.model;

import model.Anggota;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnggotaTest {

    @Test
    void testConstructor() {
        Anggota anggota = new Anggota("A001", "Budi");
        assertEquals("A001", anggota.getId());
        assertEquals("Budi", anggota.getNama());
        assertEquals(0, anggota.getPinjamanAktif());
        assertTrue(anggota.bisaPinjam());
    }

    @Test
    void testTambahPinjaman() {
        Anggota anggota = new Anggota("A001", "Budi");
        anggota.tambahPinjaman();
        assertEquals(1, anggota.getPinjamanAktif());
        assertTrue(anggota.bisaPinjam());

        anggota.tambahPinjaman();
        anggota.tambahPinjaman();
        assertEquals(3, anggota.getPinjamanAktif());
        assertFalse(anggota.bisaPinjam());
    }

    @Test
    void testTambahPinjamanMelebihiBatas() {
        Anggota anggota = new Anggota("A001", "Budi");
        anggota.tambahPinjaman();
        anggota.tambahPinjaman();
        anggota.tambahPinjaman();

        assertThrows(IllegalStateException.class, anggota::tambahPinjaman);
    }

    @Test
    void testKurangiPinjaman() {
        Anggota anggota = new Anggota("A001", "Budi");
        anggota.tambahPinjaman();
        anggota.tambahPinjaman();
        assertEquals(2, anggota.getPinjamanAktif());

        anggota.kurangiPinjaman();
        assertEquals(1, anggota.getPinjamanAktif());
    }

    @Test
    void testKurangiPinjamanMinNol() {
        Anggota anggota = new Anggota("A001", "Budi");
        anggota.kurangiPinjaman(); // seharusnya tidak jadi negatif
        assertEquals(0, anggota.getPinjamanAktif());
    }

    @Test
    void testSetter() {
        Anggota anggota = new Anggota();
        anggota.setId("A002");
        anggota.setNama("Siti");
        anggota.setPinjamanAktif(2);

        assertEquals("A002", anggota.getId());
        assertEquals("Siti", anggota.getNama());
        assertEquals(2, anggota.getPinjamanAktif());
    }

    @Test
    void testToString() {
        Anggota anggota = new Anggota("A001", "Budi");
        String str = anggota.toString();
        assertTrue(str.contains("A001"));
        assertTrue(str.contains("Budi"));
        assertTrue(str.contains("0/3"));
    }

    @Test
    void testBisaPinjamEdge() {
        Anggota anggota = new Anggota("A001", "Budi");
        // 0 -> 1 -> 2 -> 3 (penuh)
        for (int i = 0; i < 3; i++) {
            assertTrue(anggota.bisaPinjam());
            anggota.tambahPinjaman();
        }
        assertFalse(anggota.bisaPinjam());
    }
}
