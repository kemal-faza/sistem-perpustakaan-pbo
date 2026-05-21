package unit.model;

import model.Peminjaman;
import model.StatusPeminjaman;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PeminjamanTest {

    private final LocalDate today = LocalDate.of(2026, 5, 15);

    @Test
    void testConstructor() {
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                today, today.plusDays(7));

        assertEquals("P001", p.getId());
        assertEquals("P001", p.getIdPeminjaman());
        assertEquals("A001", p.getIdAnggota());
        assertEquals("B001", p.getIdItem());
        assertEquals(today, p.getTanggalPinjam());
        assertEquals(today.plusDays(7), p.getTanggalKembali());
        assertEquals(StatusPeminjaman.DIPINJAM, p.getStatus());
        assertEquals(0.0, p.getDenda());
        assertEquals(0, p.getJumlahPerpanjang());
    }

    @Test
    void testKembalikanTepatWaktu() {
        LocalDate now = LocalDate.now();
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                now.minusDays(5), now);
        int hariTerlambat = p.kembalikan();
        
        assertEquals(0, hariTerlambat);
        assertEquals(StatusPeminjaman.DIKEMBALIKAN, p.getStatus());
        assertEquals(now, p.getTanggalDikembalikan());
    }

    @Test
    void testKembalikanTerlambat() {
        LocalDate now = LocalDate.now();
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                now.minusDays(10), now.minusDays(3));
        int hariTerlambat = p.kembalikan();
        
        assertEquals(3, hariTerlambat);
        assertEquals(StatusPeminjaman.TERLAMBAT, p.getStatus());
    }

    @Test
    void testKembalikanDuplikatError() {
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                today.minusDays(5), today);
        p.kembalikan();
        assertThrows(IllegalStateException.class, p::kembalikan);
    }

    @Test
    void testHitungHariTerlambatBelumKembali() {
        LocalDate now = LocalDate.now();
        // Terlambat 5 hari dan belum dikembalikan
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                now.minusDays(12), now.minusDays(5));
        int terlambat = p.hitungHariTerlambat();
        assertEquals(5, terlambat);
    }

    @Test
    void testHitungHariTerlambatBelumJatuhTempo() {
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                today, today.plusDays(7));
        assertEquals(0, p.hitungHariTerlambat());
    }

    @Test
    void testPerpanjang() {
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                today, today.plusDays(7));
        LocalDate sebelum = p.getTanggalKembali();

        p.perpanjang();
        assertEquals(sebelum.plusDays(3), p.getTanggalKembali());
        assertEquals(1, p.getJumlahPerpanjang());
    }

    @Test
    void testPerpanjangMaxLimit() {
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                today, today.plusDays(7));

        p.perpanjang(); // 1
        p.perpanjang(); // 2
        assertThrows(IllegalStateException.class, p::perpanjang);
    }

    @Test
    void testPerpanjangSetelahDikembalikan() {
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                today.minusDays(5), today);
        p.kembalikan();
        assertThrows(IllegalStateException.class, p::perpanjang);
    }

    @Test
    void testSetter() {
        Peminjaman p = new Peminjaman();
        p.setIdPeminjaman("P002");
        p.setIdAnggota("A002");
        p.setIdItem("B002");
        p.setTanggalPinjam(today);
        p.setTanggalKembali(today.plusDays(14));
        p.setTanggalDikembalikan(today.plusDays(10));
        p.setStatus(StatusPeminjaman.DIKEMBALIKAN);
        p.setDenda(5000);

        assertEquals("P002", p.getIdPeminjaman());
        assertEquals("A002", p.getIdAnggota());
        assertEquals("B002", p.getIdItem());
        assertEquals(StatusPeminjaman.DIKEMBALIKAN, p.getStatus());
        assertEquals(5000, p.getDenda());
    }

    @Test
    void testToString() {
        Peminjaman p = new Peminjaman("P001", "A001", "B001",
                today, today.plusDays(7));
        String str = p.toString();
        assertTrue(str.contains("P001"));
        assertTrue(str.contains("A001"));
    }
}
