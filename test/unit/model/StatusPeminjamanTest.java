package unit.model;

import model.StatusPeminjaman;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusPeminjamanTest {

    @Test
    void testNilaiEnum() {
        assertEquals(3, StatusPeminjaman.values().length);
        assertNotNull(StatusPeminjaman.valueOf("DIPINJAM"));
        assertNotNull(StatusPeminjaman.valueOf("DIKEMBALIKAN"));
        assertNotNull(StatusPeminjaman.valueOf("TERLAMBAT"));
    }
}
