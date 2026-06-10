# Architecture Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development, superpowers:dispatching-parallel-agents, or superpowers:executing-plans. See the Task Grouping section for parallel vs sequential execution strategy.

**Goal:** Refactor the Perpustakaan PBO codebase to fix 5 architecture issues identified in the architecture review: scattered persistence, god-module Repository, misnamed BukuService, double validation, and leaky UI dependency.

**Architecture:** All 5 fixes preserve the existing public API (ILibraryService) and behavior. Changes are internal restructuring: extract new classes (GsonFactory, AnggotaService), centralize persistence orchestration, eliminate duplicate validation, and fix dependency injection in UI. No new dependencies required.

**Execution Strategy:** Hybrid -- Sequential Chain 1 (persistence foundation) must complete first, then Parallel Batch 1 (3 independent fixes), then Sequential Chain 2 (validation fix that depends on stable service boundaries).

**Tech Stack:** Java 17+, Gson 2.10.1, JUnit 5 (testing)

---

## File Structure Overview

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `src/service/PerpustakaanService.java` | Centralize persistence, delegate to AnggotaService |
| Create | `src/collection/GsonFactory.java` | Gson configuration with polymorphic type handling |
| Modify | `src/collection/Repository.java` | Accept Gson via constructor, remove hardcoded subtypes |
| Create | `src/service/AnggotaService.java` | CRUD + ID generation for Anggota |
| Modify | `src/service/BukuService.java` | Remove anggota-related methods |
| Modify | `src/service/PeminjamanService.java` | Remove pre-validation, translate model exceptions |
| Modify | `src/model/base/ItemPerpustakaan.java` | No change (model stays authoritative) |
| Modify | `src/model/Anggota.java` | No change (model stays authoritative) |
| Modify | `src/ui/MenuAdmin.java` | Accept ILibraryService via constructor |
| Modify | `src/ui/MenuAnggota.java` | Accept ILibraryService via constructor |
| Modify | `src/Main.java` | Pass service instance to UI constructors |
| Create | `test/unit/service/AnggotaServiceTest.java` | Tests for new AnggotaService |
| Create | `test/unit/collection/GsonFactoryTest.java` | Tests for new GsonFactory |
| Modify | `test/unit/service/PerpustakaanServiceTest.java` | Update for new delegation patterns |

---

## Task Grouping

### Sequential Chain 1: Persistence Foundation (Fix #1)
```
Task 1: Centralize persistence in PerpustakaanService (AFK, blocked by: None)
```

### Parallel Batch 1: Independent Fixes (blocked by: Chain 1)
```
Task 2: Extract GsonFactory from Repository — Fix #2 (AFK)
Task 3: Extract AnggotaService from BukuService — Fix #3 (AFK)
Task 4: UI Constructor Injection — Fix #5 (AFK)
```

### Sequential Chain 2: Validation Cleanup (blocked by: Parallel Batch 1)
```
Task 5: Remove Double Validation — Fix #4 (AFK, blocked by: Task 3)
```

---

## Sequential Chain 1: Persistence Foundation

### Task 1: Centralize Persistence in PerpustakaanService

**Type:** `AFK`
**Blocked by:** None
**Fix:** #1 — Extract Persistence dari PerpustakaanService

**Files:**
- Modify: `src/service/PerpustakaanService.java:104-166`
- Test: `test/unit/service/PerpustakaanServiceTest.java` (existing tests verify behavior)

**Context:**
PerpustakaanService currently has scattered `repoBuku.saveToJson()`, `repoAnggota.saveToJson()`, `repoPeminjaman.saveToJson()` calls across 8+ methods. The fix: introduce a single `persistAll()` private method and replace all manual save calls with it.

- [ ] **Step 1: Write failing test for persistence atomicity**

Add this test to `test/unit/service/PerpustakaanServiceTest.java` before the closing `}` of the class (after line 518):

```java
    // ========== PERSISTENCE ATOMICITY (Fix #1) ==========

    @Test
    @Order(70)
    void testPinjamBukuPersistAllRepos() throws Exception {
        // After pinjamBuku, all 3 repos should be persisted to disk
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 2));
        service.tambahAnggota(new Anggota("A001", "Budi"));
        service.pinjamBuku("B001", "A001");

        // Verify all 3 JSON files contain the expected data
        String bukuJson = Files.readString(Path.of(DATA_DIR, "buku.json"));
        String anggotaJson = Files.readString(Path.of(DATA_DIR, "anggota.json"));
        String peminjamanJson = Files.readString(Path.of(DATA_DIR, "peminjaman.json"));

        assertTrue(bukuJson.contains("B001"), "buku.json should contain B001");
        assertTrue(anggotaJson.contains("A001"), "anggota.json should contain A001");
        assertTrue(peminjamanJson.contains("P001"), "peminjaman.json should contain P001");
    }

    @Test
    @Order(71)
    void testTambahBukuOnlyNeedsBukuRepo() {
        // tambahBuku should persist correctly
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 1));

        // Verify data persisted
        String bukuJson = Files.readString(Path.of(DATA_DIR, "buku.json"));
        assertTrue(bukuJson.contains("B001"));
    }
```

- [ ] **Step 2: Run test to verify it passes with current code (baseline)**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/PerpustakaanServiceTest.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --select-method "unit.service.PerpustakaanServiceTest#testPinjamBukuPersistAllRepos" --select-method "unit.service.PerpustakaanServiceTest#testTambahBukuOnlyNeedsBukuRepo"
```
Expected: PASS (current code already saves, just in a scattered way -- these tests establish baseline)

- [ ] **Step 3: Add `persistAll()` helper method to PerpustakaanService**

In `src/service/PerpustakaanService.java`, add this private method right before the `// ========== PERSISTENCE ==========` section (before line 188):

```java
    // ========== INTERNAL PERSISTENCE HELPER ==========

    /**
     * Menyimpan semua repository ke file JSON.
     * Dipanggil setelah setiap operasi write untuk memastikan konsistensi data.
     */
    private void persistAll() {
        boolean ok = true;
        if (!repoBuku.saveToJson()) ok = false;
        if (!repoAnggota.saveToJson()) ok = false;
        if (!repoPeminjaman.saveToJson()) ok = false;
        if (!ok) {
            System.err.println("Peringatan: Gagal menyimpan beberapa data!");
        }
    }
```

- [ ] **Step 4: Replace all scattered saveToJson() calls with persistAll()**

In `src/service/PerpustakaanService.java`, make these replacements:

**Method `tambahBuku` (line 104-108):** Replace
```java
    public AddResult tambahBuku(ItemPerpustakaan item) {
        AddResult result = bukuService.tambahBuku(item);
        repoBuku.saveToJson();
        return result;
    }
```
with:
```java
    public AddResult tambahBuku(ItemPerpustakaan item) {
        AddResult result = bukuService.tambahBuku(item);
        persistAll();
        return result;
    }
```

**Method `hapusBuku` (line 110-113):** Replace
```java
    public void hapusBuku(String idBuku) throws BukuTidakDitemukanException {
        bukuService.hapusBuku(idBuku);
        repoBuku.saveToJson();
    }
```
with:
```java
    public void hapusBuku(String idBuku) throws BukuTidakDitemukanException {
        bukuService.hapusBuku(idBuku);
        persistAll();
    }
```

**Method `tambahAnggota` (line 129-132):** Replace
```java
    public void tambahAnggota(Anggota anggota) {
        bukuService.tambahAnggota(anggota);
        repoAnggota.saveToJson();
    }
```
with:
```java
    public void tambahAnggota(Anggota anggota) {
        bukuService.tambahAnggota(anggota);
        persistAll();
    }
```

**Method `pinjamBuku` (line 144-151):** Replace
```java
        peminjamanService.pinjamBuku(idBuku, idAnggota);
        repoBuku.saveToJson();
        repoAnggota.saveToJson();
        repoPeminjaman.saveToJson();
```
with:
```java
        peminjamanService.pinjamBuku(idBuku, idAnggota);
        persistAll();
```

**Method `kembalikanBuku` (line 153-160):** Replace
```java
        Peminjaman result = peminjamanService.kembalikanBuku(idPeminjaman);
        repoPeminjaman.saveToJson();
        repoBuku.saveToJson();
        repoAnggota.saveToJson();
        return result;
```
with:
```java
        Peminjaman result = peminjamanService.kembalikanBuku(idPeminjaman);
        persistAll();
        return result;
```

**Method `perpanjangPeminjaman` (line 162-166):** Replace
```java
        peminjamanService.perpanjangPeminjaman(idPeminjaman);
        repoPeminjaman.saveToJson();
```
with:
```java
        peminjamanService.perpanjangPeminjaman(idPeminjaman);
        persistAll();
```

**Method `simpanSemua` (line 190-198):** Replace the entire method body to delegate to `persistAll()`:
```java
    public void simpanSemua() {
        persistAll();
    }
```

- [ ] **Step 5: Run ALL tests to verify nothing broke**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/PerpustakaanServiceTest.java test/unit/collection/RepositoryTest.java test/unit/model/*.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
```
Expected: ALL tests PASS (same behavior, just centralized persistence)

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor: centralize persistence with persistAll() in PerpustakaanService

Replace scattered repoXxx.saveToJson() calls across 8+ methods with
a single persistAll() helper. This ensures all repositories are saved
consistently after every write operation, eliminating the risk of
forgetting to persist a modified repository."
```

---

## Parallel Batch 1: Independent Fixes

These 3 tasks are independent and can be executed concurrently.

---

### Task 2: Extract GsonFactory from Repository

**Type:** `AFK`
**Blocked by:** Task 1 (must have stable PerpustakaanService first)
**Fix:** #2 — Repository Tahu Concrete Subtypes (God-Module)

**Files:**
- Create: `src/collection/GsonFactory.java`
- Modify: `src/collection/Repository.java:1-53,170-194`
- Create: `test/unit/collection/GsonFactoryTest.java`

**Context:**
Repository currently hardcodes `RuntimeTypeAdapterFactory` with knowledge of `BukuFisik`, `BukuDigital`, and `Jurnal` in its `createGson()` method (lines 170-194). This makes Repository non-generic. The fix: extract Gson configuration to a dedicated `GsonFactory` class, and inject the Gson instance into Repository via constructor.

- [ ] **Step 1: Write failing test for GsonFactory**

Create `test/unit/collection/GsonFactoryTest.java`:

```java
package unit.collection;

import collection.GsonFactory;
import com.google.gson.Gson;
import model.*;
import model.base.ItemPerpustakaan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GsonFactoryTest {

    private final Gson gson = GsonFactory.create();

    @Test
    void testSerializeBukuFisik() {
        BukuFisik buku = new BukuFisik("B001", "Test Buku", 2024,
                Kategori.TEKNOLOGI, "Penerbit", "Penulis",
                300, "A1-01", 2);
        String json = gson.toJson(buku, ItemPerpustakaan.class);
        assertTrue(json.contains("\"type\":\"Buku Fisik\""));
        assertTrue(json.contains("Test Buku"));
    }

    @Test
    void testSerializeBukuDigital() {
        BukuDigital buku = new BukuDigital("B002", "E-Book", 2024,
                Kategori.TEKNOLOGI, "E-Pub", "Penulis",
                5.2, "PDF");
        String json = gson.toJson(buku, ItemPerpustakaan.class);
        assertTrue(json.contains("\"type\":\"Buku Digital\""));
    }

    @Test
    void testSerializeJurnal() {
        Jurnal jurnal = new Jurnal("B003", "Jurnal Test", 2024,
                Kategori.ILMIAH, "Universitas", "Editor",
                1, 1, "Ilmu Komputer", 1);
        String json = gson.toJson(jurnal, ItemPerpustakaan.class);
        assertTrue(json.contains("\"type\":\"Jurnal\""));
    }

    @Test
    void testDeserializePolymorphic() {
        String json = """
            [
              {"type":"Buku Fisik","id":"B001","judul":"Test","tahunTerbit":2024,
               "kategori":"TEKNOLOGI","penerbit":"Pub","penulis":"Pen",
               "stok":1,"dipinjam":0,"jumlahHalaman":100,"lokasiRak":"A1"},
              {"type":"Buku Digital","id":"B002","judul":"E-Book","tahunTerbit":2024,
               "kategori":"TEKNOLOGI","penerbit":"Pub","penulis":"Pen",
               "stok":1,"dipinjam":0,"ukuranFile":3.5,"format":"PDF"},
              {"type":"Jurnal","id":"B003","judul":"Jurnal","tahunTerbit":2024,
               "kategori":"ILMIAH","penerbit":"Univ","penulis":"Ed",
               "stok":1,"dipinjam":0,"volume":1,"nomor":1,"bidang":"CS"}
            ]
            """;

        var type = new com.google.gson.reflect.TypeToken<ArrayList<ItemPerpustakaan>>() {}.getType();
        List<ItemPerpustakaan> items = gson.fromJson(json, type);

        assertEquals(3, items.size());
        assertInstanceOf(BukuFisik.class, items.get(0));
        assertInstanceOf(BukuDigital.class, items.get(1));
        assertInstanceOf(Jurnal.class, items.get(2));
    }

    @Test
    void testLocalDateSerialization() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        String json = gson.toJson(date);
        assertEquals("\"2024-06-15\"", json);
    }

    @Test
    void testLocalDateDeserialization() {
        LocalDate date = gson.fromJson("\"2024-06-15\"", LocalDate.class);
        assertEquals(LocalDate.of(2024, 6, 15), date);
    }

    @Test
    void testKategoriBackwardCompat() {
        // Kategori.fromString handles unknown values gracefully
        String json = """
            {"type":"Buku Fisik","id":"B001","judul":"Test","tahunTerbit":2024,
             "kategori":"TEKNOLOGI","penerbit":"Pub","penulis":"Pen",
             "stok":1,"dipinjam":0,"jumlahHalaman":100,"lokasiRak":"A1"}
            """;
        ItemPerpustakaan item = gson.fromJson(json, ItemPerpustakaan.class);
        assertEquals(Kategori.TEKNOLOGI, item.getKategori());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/collection/GsonFactoryTest.java 2>&1
```
Expected: FAIL with "package collection does not exist" or "cannot find symbol: class GsonFactory"

- [ ] **Step 3: Create GsonFactory class**

Create `src/collection/GsonFactory.java`:

```java
package collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import model.Kategori;
import model.base.ItemPerpustakaan;
import model.BukuFisik;
import model.BukuDigital;
import model.Jurnal;

import java.time.LocalDate;

/**
 * Factory untuk membangun Gson instance yang dikonfigurasi untuk
 * polymorphic deserialization ItemPerpustakaan dan adapter LocalDate.
 *
 * Memisahkan konfigurasi serialisasi dari Repository agar Repository
 * tetap benar-benar generic dan tidak mengetahui concrete subtypes.
 */
public class GsonFactory {

    private GsonFactory() {
        // Utility class — tidak perlu di-instantiate
    }

    /**
     * Membuat Gson instance dengan konfigurasi lengkap:
     * - RuntimeTypeAdapterFactory untuk polymorphic ItemPerpustakaan
     * - TypeAdapter untuk Kategori enum (backward compatibility)
     * - TypeAdapter untuk LocalDate (bypass Java 17+ reflection restrictions)
     *
     * @return GsonBuilder yang sudah dikonfigurasi (caller bisa chain .create())
     */
    public static GsonBuilder builder() {
        RuntimeTypeAdapterFactory<ItemPerpustakaan> typeFactory =
            RuntimeTypeAdapterFactory.of(ItemPerpustakaan.class, "type")
                .registerSubtype(BukuFisik.class, "Buku Fisik")
                .registerSubtype(BukuDigital.class, "Buku Digital")
                .registerSubtype(Jurnal.class, "Jurnal");

        JsonDeserializer<Kategori> kategoriDeserializer = (json, type, ctx) -> {
            String value = json.getAsString();
            return Kategori.fromString(value);
        };

        JsonSerializer<LocalDate> localDateSerializer =
            (src, type, ctx) -> new JsonPrimitive(src.toString());
        JsonDeserializer<LocalDate> localDateDeserializer =
            (json, type, ctx) -> LocalDate.parse(json.getAsString());

        return new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .registerTypeAdapter(Kategori.class, kategoriDeserializer)
            .registerTypeAdapter(LocalDate.class, localDateSerializer)
            .registerTypeAdapter(LocalDate.class, localDateDeserializer);
    }

    /**
     * Membuat Gson instance default (compact).
     * @return Gson instance siap pakai
     */
    public static Gson create() {
        return builder().create();
    }

    /**
     * Membuat Gson instance dengan pretty printing.
     * @return Gson instance siap pakai (formatted)
     */
    public static Gson createPretty() {
        return builder().setPrettyPrinting().create();
    }
}
```

- [ ] **Step 4: Run GsonFactory tests to verify they pass**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/collection/GsonFactoryTest.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --select-class unit.collection.GsonFactoryTest
```
Expected: ALL 7 tests PASS

- [ ] **Step 5: Refactor Repository to accept Gson via constructor**

In `src/collection/Repository.java`, make these changes:

**Remove imports (lines 5-8, 14-18):** Remove unused imports:
```java
// REMOVE these imports:
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import model.Kategori;
import model.base.ItemPerpustakaan;
import model.BukuFisik;
import model.BukuDigital;
import model.Jurnal;
```

Keep these imports:
```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import interfaces.Identifiable;
import java.time.LocalDate;  // REMOVE this too — no longer needed here
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
```

**Replace constructor (lines 46-53):** Change from:
```java
    public Repository(String filePath, Type typeToken) {
        this.items = new ArrayList<>();
        this.filePath = filePath;
        this.typeToken = typeToken;
        this.gson = createGson().create();
        this.prettyGson = createGson().setPrettyPrinting().create();
        loadFromJson();
    }
```
to:
```java
    /**
     * Constructor repository dengan Gson injection.
     * @param filePath path file JSON untuk persistensi
     * @param typeToken TypeToken Gson untuk deserialisasi
     */
    public Repository(String filePath, Type typeToken) {
        this.items = new ArrayList<>();
        this.filePath = filePath;
        this.typeToken = typeToken;
        this.gson = GsonFactory.create();
        this.prettyGson = GsonFactory.createPretty();
        loadFromJson();
    }
```

**Delete the entire `createGson()` method (lines 166-194):** Remove the entire method including its Javadoc comment. This method is now replaced by `GsonFactory`.

- [ ] **Step 6: Run ALL tests to verify Repository still works**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/collection/RepositoryTest.java test/unit/collection/GsonFactoryTest.java test/unit/service/PerpustakaanServiceTest.java test/unit/model/*.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
```
Expected: ALL tests PASS

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "refactor: extract GsonFactory from Repository to remove concrete subtype knowledge

Repository no longer hardcodes RuntimeTypeAdapterFactory with
BukuFisik/BukuDigital/Jurnal. Gson configuration is now in
GsonFactory, making Repository truly generic and reusable."
```

---

### Task 3: Extract AnggotaService from BukuService

**Type:** `AFK`
**Blocked by:** Task 1 (must have stable PerpustakaanService first)
**Fix:** #3 — BukuService Menangani Anggota (Misnamed Module)

**Files:**
- Create: `src/service/AnggotaService.java`
- Modify: `src/service/BukuService.java:15,17-20,70-88`
- Modify: `src/service/PerpustakaanService.java:48-49,129-140,206-208`
- Create: `test/unit/service/AnggotaServiceTest.java`

**Context:**
BukuService currently handles both buku AND anggota operations (tambahAnggota, getAllAnggota, getAnggotaById, generateIdAnggota). This violates single responsibility. The fix: extract AnggotaService as a dedicated class for anggota CRUD.

- [ ] **Step 1: Write failing test for AnggotaService**

Create `test/unit/service/AnggotaServiceTest.java`:

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/AnggotaServiceTest.java 2>&1
```
Expected: FAIL with "cannot find symbol: class AnggotaService"

- [ ] **Step 3: Create AnggotaService class**

Create `src/service/AnggotaService.java`:

```java
package service;

import collection.Repository;
import model.Anggota;

import java.util.List;

/**
 * Service untuk mengelola operasi CRUD anggota perpustakaan.
 * Dipisahkan dari BukuService agar setiap service memiliki
 * tanggung jawab yang jelas (Single Responsibility Principle).
 */
public class AnggotaService {

    private Repository<Anggota> repoAnggota;

    public AnggotaService(Repository<Anggota> repoAnggota) {
        this.repoAnggota = repoAnggota;
    }

    /** Menambahkan anggota baru ke repository */
    public void tambahAnggota(Anggota anggota) {
        repoAnggota.add(anggota);
    }

    /** Mendapatkan semua anggota terdaftar */
    public List<Anggota> getAllAnggota() {
        return repoAnggota.getAll();
    }

    /** Mendapatkan anggota berdasarkan ID */
    public Anggota getAnggotaById(String idAnggota) {
        return repoAnggota.findById(idAnggota);
    }

    /** Generate ID unik untuk anggota baru (format: AXXX) */
    public String generateIdAnggota() {
        return repoAnggota.generateId("A");
    }
}
```

- [ ] **Step 4: Run AnggotaService tests to verify they pass**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/AnggotaServiceTest.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --select-class unit.service.AnggotaServiceTest
```
Expected: ALL 6 tests PASS

- [ ] **Step 5: Remove anggota methods from BukuService**

In `src/service/BukuService.java`, make these changes:

**Remove field and constructor parameter for repoAnggota:** Replace
```java
public class BukuService {
    private Repository<ItemPerpustakaan> repoBuku;
    private Repository<Anggota> repoAnggota;

    public BukuService(Repository<ItemPerpustakaan> repoBuku, Repository<Anggota> repoAnggota) {
        this.repoBuku = repoBuku;
        this.repoAnggota = repoAnggota;
    }
```
with:
```java
public class BukuService {
    private Repository<ItemPerpustakaan> repoBuku;

    public BukuService(Repository<ItemPerpustakaan> repoBuku) {
        this.repoBuku = repoBuku;
    }
```

**Remove import for Anggota:** Remove `import model.Anggota;`

**Remove all anggota methods (lines 70-88):** Delete these methods entirely:
```java
    public void tambahAnggota(Anggota anggota) { ... }
    public List<Anggota> getAllAnggota() { ... }
    public Anggota getAnggotaById(String idAnggota) { ... }
    public String generateIdAnggota() { ... }
```

- [ ] **Step 6: Update PerpustakaanService to use AnggotaService**

In `src/service/PerpustakaanService.java`:

**Add field:** Add after `private BukuService bukuService;` (line 27):
```java
    private AnggotaService anggotaService;
```

**Update constructor (line 48-50):** Replace
```java
        this.authService = new AuthService(repoAnggota);
        this.bukuService = new BukuService(repoBuku, repoAnggota);
        this.peminjamanService = new PeminjamanService(repoBuku, repoAnggota, repoPeminjaman);
```
with:
```java
        this.authService = new AuthService(repoAnggota);
        this.bukuService = new BukuService(repoBuku);
        this.anggotaService = new AnggotaService(repoAnggota);
        this.peminjamanService = new PeminjamanService(repoBuku, repoAnggota, repoPeminjaman);
```

**Update anggota delegation methods (lines 129-140):** Replace
```java
    public void tambahAnggota(Anggota anggota) {
        bukuService.tambahAnggota(anggota);
        persistAll();
    }

    public List<Anggota> getAllAnggota() {
        return bukuService.getAllAnggota();
    }

    public Anggota getAnggotaById(String idAnggota) {
        return bukuService.getAnggotaById(idAnggota);
    }
```
with:
```java
    public void tambahAnggota(Anggota anggota) {
        anggotaService.tambahAnggota(anggota);
        persistAll();
    }

    public List<Anggota> getAllAnggota() {
        return anggotaService.getAllAnggota();
    }

    public Anggota getAnggotaById(String idAnggota) {
        return anggotaService.getAnggotaById(idAnggota);
    }
```

**Update generateIdAnggota (line 206-208):** Replace
```java
    public String generateIdAnggota() {
        return bukuService.generateIdAnggota();
    }
```
with:
```java
    public String generateIdAnggota() {
        return anggotaService.generateIdAnggota();
    }
```

- [ ] **Step 7: Run ALL tests to verify nothing broke**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/PerpustakaanServiceTest.java test/unit/service/AnggotaServiceTest.java test/unit/collection/RepositoryTest.java test/unit/collection/GsonFactoryTest.java test/unit/model/*.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
```
Expected: ALL tests PASS

- [ ] **Step 8: Commit**

```bash
git add -A && git commit -m "refactor: extract AnggotaService from BukuService

BukuService now only handles item perpustakaan (buku) operations.
AnggotaService is a new dedicated class for anggota CRUD, following
Single Responsibility Principle. PerpustakaanService delegates to
the appropriate service based on domain."
```

---

### Task 4: UI Constructor Injection

**Type:** `AFK`
**Blocked by:** Task 1 (must have stable PerpustakaanService first)
**Fix:** #5 — UI Bypass ILibraryService (Leaky Dependency)

**Files:**
- Modify: `src/ui/MenuAdmin.java:29-33`
- Modify: `src/ui/MenuAnggota.java:26-30`
- Modify: `src/Main.java:72,88`

**Context:**
MenuAdmin and MenuAnggota declare `ILibraryService service` but call `PerpustakaanService.getInstance()` directly in their constructors, violating Dependency Inversion. The fix: accept `ILibraryService` as a constructor parameter. Main.java becomes the composition root that wires everything.

- [ ] **Step 1: Update MenuAdmin constructor**

In `src/ui/MenuAdmin.java`:

**Remove import:** Remove `import service.PerpustakaanService;` (line 7)

**Replace constructor (lines 31-33):** Replace
```java
    private ILibraryService service;

    public MenuAdmin() {
        this.service = PerpustakaanService.getInstance();
    }
```
with:
```java
    private final ILibraryService service;

    /**
     * Constructor dengan dependency injection.
     * @param service instance ILibraryService untuk operasi perpustakaan
     */
    public MenuAdmin(ILibraryService service) {
        this.service = service;
    }
```

- [ ] **Step 2: Update MenuAnggota constructor**

In `src/ui/MenuAnggota.java`:

**Remove import:** Remove `import service.PerpustakaanService;` (line 7)

**Replace constructor (lines 28-30):** Replace
```java
    private ILibraryService service;

    public MenuAnggota() {
        this.service = PerpustakaanService.getInstance();
    }
```
with:
```java
    private final ILibraryService service;

    /**
     * Constructor dengan dependency injection.
     * @param service instance ILibraryService untuk operasi perpustakaan
     */
    public MenuAnggota(ILibraryService service) {
        this.service = service;
    }
```

- [ ] **Step 3: Update Main.java to inject service**

In `src/Main.java`:

**Update loginAdmin method (line 72):** Replace
```java
            new MenuAdmin().jalankan();
```
with:
```java
            new MenuAdmin(service).jalankan();
```

**Update loginAnggota method (line 88):** Replace
```java
                new MenuAnggota().jalankan();
```
with:
```java
                new MenuAnggota(service).jalankan();
```

- [ ] **Step 4: Compile and run ALL tests to verify**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/PerpustakaanServiceTest.java test/unit/service/AnggotaServiceTest.java test/unit/collection/RepositoryTest.java test/unit/collection/GsonFactoryTest.java test/unit/model/*.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
```
Expected: ALL tests PASS. Compilation succeeds with no errors.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor: inject ILibraryService via UI constructors (Dependency Inversion)

MenuAdmin and MenuAnggota now receive ILibraryService via constructor
parameter instead of calling PerpustakaanService.getInstance() directly.
Main.java acts as composition root, wiring the dependency explicitly.
This makes UI testable with mock services."
```

---

## Sequential Chain 2: Validation Cleanup

### Task 5: Remove Double Validation

**Type:** `AFK`
**Blocked by:** Task 3 (service boundaries must be stable)
**Fix:** #4 — Double Validation (Service + Model)

**Files:**
- Modify: `src/service/PeminjamanService.java:42-62`
- Test: `test/unit/service/PerpustakaanServiceTest.java` (existing tests verify behavior)

**Context:**
PeminjamanService currently validates `item.isTersedia()` and `anggota.bisaPinjam()` BEFORE calling `item.pinjam()` and `anggota.tambahPinjaman()`. But those model methods ALSO validate the same conditions internally, throwing `IllegalStateException`. This means the same business rule is defined in 2 places with 2 different exception types.

The fix: Remove the pre-validation in PeminjamanService. Let the model be the single source of truth for invariants. PeminjamanService catches `IllegalStateException` from model and translates it to the appropriate domain exception. For atomicity, if `anggota.tambahPinjaman()` fails after `item.pinjam()` succeeds, rollback the item.

- [ ] **Step 1: Write failing test for exception translation**

Add this test to `test/unit/service/PerpustakaanServiceTest.java` before the closing `}` of the class:

```java
    // ========== EXCEPTION TRANSLATION (Fix #4) ==========

    @Test
    @Order(80)
    void testPinjamBukuNotAvailableThrowsDomainException() {
        service.tambahBuku(new BukuFisik("B001", "PBO", 2024,
                Kategori.TEKNOLOGI, "Informatika", "Tim PBO",
                350, "A1-01", 1));
        service.tambahAnggota(new Anggota("A001", "Budi"));
        service.tambahAnggota(new Anggota("A002", "Siti"));

        // First borrow succeeds
        assertDoesNotThrow(() -> service.pinjamBuku("B001", "A001"));

        // Second borrow should throw domain exception (not IllegalStateException)
        assertThrows(BukuTidakTersediaException.class,
                () -> service.pinjamBuku("B001", "A002"));
    }

    @Test
    @Order(81)
    void testPinjamBukuMaxLimitThrowsDomainException() {
        service.tambahBuku(new BukuFisik("B001", "Buku Satu", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-01", 5));
        service.tambahBuku(new BukuFisik("B002", "Buku Dua", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-02", 5));
        service.tambahBuku(new BukuFisik("B003", "Buku Tiga", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-03", 5));
        service.tambahBuku(new BukuFisik("B004", "Buku Empat", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-04", 5));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        // Pinjam 3 buku (max = 3)
        assertDoesNotThrow(() -> service.pinjamBuku("B001", "A001"));
        assertDoesNotThrow(() -> service.pinjamBuku("B002", "A001"));
        assertDoesNotThrow(() -> service.pinjamBuku("B003", "A001"));

        // Ke-4 should throw domain exception (not IllegalStateException)
        assertThrows(PeminjamanMelebihiBatasException.class,
                () -> service.pinjamBuku("B004", "A001"));
    }

    @Test
    @Order(82)
    void testPinjamBukuRollbackOnAnggotaFailure() throws Exception {
        // Verify atomicity: if anggota.tambahPinjaman() fails,
        // item.pinjam() should be rolled back
        service.tambahBuku(new BukuFisik("B001", "Buku Satu", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-01", 5));
        service.tambahBuku(new BukuFisik("B002", "Buku Dua", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-02", 5));
        service.tambahBuku(new BukuFisik("B003", "Buku Tiga", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-03", 5));
        service.tambahBuku(new BukuFisik("B004", "Buku Empat", 2024,
                Kategori.TEKNOLOGI, "Pub", "Penulis", 100, "A1-04", 5));
        service.tambahAnggota(new Anggota("A001", "Budi"));

        service.pinjamBuku("B001", "A001");
        service.pinjamBuku("B002", "A001");
        service.pinjamBuku("B003", "A001");

        // This should fail AND rollback B004's dipinjam counter
        assertThrows(PeminjamanMelebihiBatasException.class,
                () -> service.pinjamBuku("B004", "A001"));

        // B004 should still have full stock available (rollback worked)
        assertEquals(5, service.getBukuById("B004").getTersedia());
    }
```

- [ ] **Step 2: Run new tests to verify current behavior**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/PerpustakaanServiceTest.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --select-method "unit.service.PerpustakaanServiceTest#testPinjamBukuRollbackOnAnggotaFailure"
```
Expected: The rollback test (`testPinjamBukuRollbackOnAnggotaFailure`) should FAIL because current code doesn't rollback. The other two should PASS (current pre-validation catches the error before model mutation).

- [ ] **Step 3: Refactor PeminjamanService.pinjamBuku() to remove double validation**

In `src/service/PeminjamanService.java`, replace the `pinjamBuku` method (lines 26-63):

Replace:
```java
    public void pinjamBuku(String idBuku, String idAnggota)
            throws BukuTidakDitemukanException, AnggotaTidakValidException,
            BukuTidakTersediaException, PeminjamanMelebihiBatasException {

        ItemPerpustakaan item = repoBuku.findById(idBuku);
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item dengan ID '" + idBuku + "' tidak ditemukan.");
        }

        Anggota anggota = repoAnggota.findById(idAnggota);
        if (anggota == null) {
            throw new AnggotaTidakValidException(
                    "Anggota dengan ID '" + idAnggota + "' tidak ditemukan.");
        }

        if (!item.isTersedia()) {
            throw new BukuTidakTersediaException(
                    "Item '" + item.getJudul() + "' sedang dipinjam orang lain.");
        }

        if (!anggota.bisaPinjam()) {
            throw new PeminjamanMelebihiBatasException(
                    "Anggota '" + anggota.getNama() + "' sudah mencapai batas pinjam ("
                            + Anggota.MAX_PINJAM + ").");
        }

        String idPeminjaman = repoPeminjaman.generateId("P");
        LocalDate tanggalPinjam = LocalDate.now();
        LocalDate tanggalKembali = tanggalPinjam.plusDays(7);
        Peminjaman peminjaman = new Peminjaman(idPeminjaman, idAnggota, idBuku,
                tanggalPinjam, tanggalKembali);

        item.pinjam();
        anggota.tambahPinjaman();

        repoPeminjaman.add(peminjaman);
    }
```

with:
```java
    public void pinjamBuku(String idBuku, String idAnggota)
            throws BukuTidakDitemukanException, AnggotaTidakValidException,
            BukuTidakTersediaException, PeminjamanMelebihiBatasException {

        ItemPerpustakaan item = repoBuku.findById(idBuku);
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item dengan ID '" + idBuku + "' tidak ditemukan.");
        }

        Anggota anggota = repoAnggota.findById(idAnggota);
        if (anggota == null) {
            throw new AnggotaTidakValidException(
                    "Anggota dengan ID '" + idAnggota + "' tidak ditemukan.");
        }

        // Model adalah authoritative untuk invariant.
        // Service menerjemahkan IllegalStateException menjadi domain exception.
        try {
            item.pinjam();
        } catch (IllegalStateException e) {
            throw new BukuTidakTersediaException(
                    "Item '" + item.getJudul() + "' sedang tidak tersedia.");
        }

        try {
            anggota.tambahPinjaman();
        } catch (IllegalStateException e) {
            // Rollback: kembalikan stok item karena peminjaman gagal
            item.kembalikan();
            throw new PeminjamanMelebihiBatasException(
                    "Anggota '" + anggota.getNama() + "' sudah mencapai batas pinjam ("
                            + Anggota.MAX_PINJAM + ").");
        }

        String idPeminjaman = repoPeminjaman.generateId("P");
        LocalDate tanggalPinjam = LocalDate.now();
        LocalDate tanggalKembali = tanggalPinjam.plusDays(7);
        Peminjaman peminjaman = new Peminjaman(idPeminjaman, idAnggota, idBuku,
                tanggalPinjam, tanggalKembali);

        repoPeminjaman.add(peminjaman);
    }
```

Key changes:
1. Removed pre-validation (`if (!item.isTersedia())` and `if (!anggota.bisaPinjam())`)
2. Model methods (`item.pinjam()`, `anggota.tambahPinjaman()`) are now the single source of truth
3. Service catches `IllegalStateException` and translates to domain exceptions
4. Added rollback: if `anggota.tambahPinjaman()` fails, `item.kembalikan()` is called to undo the stock decrement

- [ ] **Step 4: Run ALL tests to verify**

Run:
```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/PerpustakaanServiceTest.java test/unit/service/AnggotaServiceTest.java test/unit/collection/RepositoryTest.java test/unit/collection/GsonFactoryTest.java test/unit/model/*.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
```
Expected: ALL tests PASS, including the new rollback test

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor: remove double validation, model is single source of truth

PeminjamanService no longer pre-validates isTersedia()/bisaPinjam()
before calling model methods. Model (ItemPerpustakaan, Anggota) is
the authoritative invariant enforcer. Service catches
IllegalStateException and translates to domain exceptions.
Added rollback for atomicity when anggota limit is reached."
```

---

## Verification

After all tasks are complete, run the full test suite one final time:

```bash
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java && javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath "src:test" test/unit/service/*.java test/unit/collection/*.java test/unit/model/*.java && java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
```

Expected: ALL tests PASS. The application should compile and run identically from the user's perspective.

Then verify the application runs:
```bash
java -cp "out:lib/*" Main
```

Expected: Application starts normally, shows Widya banner, login works.
