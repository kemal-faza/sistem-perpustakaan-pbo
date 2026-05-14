package unit.service;

import collection.Repository;
import com.google.gson.reflect.TypeToken;
import exception.AnggotaTidakValidException;
import model.Anggota;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private Repository<Anggota> repoAnggota;
    private String tempFile;

    @BeforeEach
    void setUp() {
        tempFile = "/tmp/opencode/test_anggota_" + System.currentTimeMillis() + ".json";
        Type type = new TypeToken<ArrayList<Anggota>>() {}.getType();
        repoAnggota = new Repository<>(tempFile, type);
        authService = new AuthService(repoAnggota);
    }

    @AfterEach
    void tearDown() {
        new File(tempFile).delete();
    }


    @Test
    void testValidateAdminCorrectCredentials() {
        assertTrue(authService.validateAdmin("admin", "admin123"));
    }

    @Test
    void testValidateAdminWrongUsername() {
        assertFalse(authService.validateAdmin("wrong", "admin123"));
    }

    @Test
    void testValidateAdminWrongPassword() {
        assertFalse(authService.validateAdmin("admin", "wrongpass"));
    }

    @Test
    void testValidateAdminBothWrong() {
        assertFalse(authService.validateAdmin("wrong", "wrongpass"));
    }

    @Test
    void testLoginAnggotaSuccess() throws AnggotaTidakValidException {
        repoAnggota.add(new Anggota("A001", "Budi"));
        Anggota anggota = authService.loginAnggota("A001");
        assertNotNull(anggota);
        assertEquals("Budi", anggota.getNama());
    }

    @Test
    void testLoginAnggotaNotFound() {
        assertThrows(AnggotaTidakValidException.class,
                () -> authService.loginAnggota("A999"));
    }

    @Test
    void testGetAdmin() {
        assertNotNull(authService.getAdmin());
        assertEquals("ADM001", authService.getAdmin().getId());
    }

    @Test
    void testLoginAnggotaMultiple() throws AnggotaTidakValidException {
        repoAnggota.add(new Anggota("A001", "Budi"));
        repoAnggota.add(new Anggota("A002", "Siti"));

        assertEquals("Budi", authService.loginAnggota("A001").getNama());
        assertEquals("Siti", authService.loginAnggota("A002").getNama());
    }
}
