package unit.service;

import collection.Repository;
import com.google.gson.reflect.TypeToken;
import model.Anggota;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AnggotaService;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AnggotaServiceTest {

    private AnggotaService anggotaService;
    private Repository<Anggota> repoAnggota;
    private String tempFile;

    @BeforeEach
    void setUp() {
        tempFile = "/tmp/opencode/test_anggota_svc_" + System.currentTimeMillis() + ".json";
        Type type = new TypeToken<ArrayList<Anggota>>() {}.getType();
        repoAnggota = new Repository<>(tempFile, type);
        anggotaService = new AnggotaService(repoAnggota);
    }

    @AfterEach
    void tearDown() {
        new File(tempFile).delete();
    }

    @Test
    void testTambahAnggota() {
        Anggota anggota = new Anggota("A001", "Budi");
        anggotaService.tambahAnggota(anggota);
        assertEquals(1, anggotaService.getAllAnggota().size());
    }

    @Test
    void testGetAllAnggota() {
        anggotaService.tambahAnggota(new Anggota("A001", "Budi"));
        anggotaService.tambahAnggota(new Anggota("A002", "Siti"));
        assertEquals(2, anggotaService.getAllAnggota().size());
    }

    @Test
    void testGetAnggotaByIdFound() {
        anggotaService.tambahAnggota(new Anggota("A001", "Budi"));
        Anggota found = anggotaService.getAnggotaById("A001");
        assertNotNull(found);
        assertEquals("Budi", found.getNama());
    }

    @Test
    void testGetAnggotaByIdNotFound() {
        assertNull(anggotaService.getAnggotaById("A999"));
    }

    @Test
    void testGenerateIdAnggota() {
        String id = anggotaService.generateIdAnggota();
        assertTrue(id.startsWith("A"));
        assertEquals(4, id.length());
    }

    @Test
    void testGenerateIdIncrement() {
        anggotaService.tambahAnggota(new Anggota("A001", "Budi"));
        String nextId = anggotaService.generateIdAnggota();
        assertEquals("A002", nextId);
    }
}
