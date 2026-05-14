package unit.model;

import model.Kategori;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KategoriTest {

    @Test
    void testNilaiEnum() {
        assertEquals(8, Kategori.values().length);
        assertNotNull(Kategori.valueOf("TEKNOLOGI"));
        assertNotNull(Kategori.valueOf("ILMIAH"));
        assertNotNull(Kategori.valueOf("FIKSI"));
        assertNotNull(Kategori.valueOf("NON_FIKSI"));
        assertNotNull(Kategori.valueOf("SEJARAH"));
        assertNotNull(Kategori.valueOf("PENDIDIKAN"));
        assertNotNull(Kategori.valueOf("REFERENSI"));
        assertNotNull(Kategori.valueOf("UMUM"));
    }

    @Test
    void testGetDisplayName() {
        assertEquals("Teknologi", Kategori.TEKNOLOGI.getDisplayName());
        assertEquals("Ilmiah", Kategori.ILMIAH.getDisplayName());
        assertEquals("Fiksi", Kategori.FIKSI.getDisplayName());
        assertEquals("Non-Fiksi", Kategori.NON_FIKSI.getDisplayName());
        assertEquals("Umum", Kategori.UMUM.getDisplayName());
    }

    @Test
    void testFromStringDisplayName() {
        assertEquals(Kategori.TEKNOLOGI, Kategori.fromString("Teknologi"));
        assertEquals(Kategori.ILMIAH, Kategori.fromString("Ilmiah"));
        assertEquals(Kategori.UMUM, Kategori.fromString("Umum"));
    }

    @Test
    void testFromStringEnumName() {
        assertEquals(Kategori.TEKNOLOGI, Kategori.fromString("TEKNOLOGI"));
        assertEquals(Kategori.FIKSI, Kategori.fromString("FIKSI"));
    }

    @Test
    void testFromStringCaseInsensitive() {
        assertEquals(Kategori.TEKNOLOGI, Kategori.fromString("teknologi"));
        assertEquals(Kategori.NON_FIKSI, Kategori.fromString("non-fiksi"));
    }

    @Test
    void testFromStringNullReturnsUmum() {
        assertEquals(Kategori.UMUM, Kategori.fromString(null));
    }

    @Test
    void testFromStringInvalidReturnsUmum() {
        assertEquals(Kategori.UMUM, Kategori.fromString("TidakAda"));
    }

    @Test
    void testToString() {
        assertEquals("Teknologi", Kategori.TEKNOLOGI.toString());
    }
}
