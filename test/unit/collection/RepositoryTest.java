package unit.collection;

import collection.Repository;
import com.google.gson.reflect.TypeToken;
import interfaces.Identifiable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    static class TestItem implements Identifiable {
        private String id;
        private String name;

        public TestItem() {}

        public TestItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private Repository<TestItem> repo;
    private String tempFile;

    @BeforeEach
    void setUp() {
        tempFile = "/tmp/opencode/test_repo_" + System.currentTimeMillis() + ".json";
        Type type = new TypeToken<ArrayList<TestItem>>() {}.getType();
        repo = new Repository<>(tempFile, type);
    }

    @AfterEach
    void tearDown() {
        new File(tempFile).delete();
    }

    @Test
    void testAdd() {
        repo.add(new TestItem("T001", "Item Satu"));
        assertEquals(1, repo.size());
    }

    @Test
    void testFindByIdFound() {
        repo.add(new TestItem("T001", "Item Satu"));
        repo.add(new TestItem("T002", "Item Dua"));

        TestItem found = repo.findById("T001");
        assertNotNull(found);
        assertEquals("Item Satu", found.getName());
    }

    @Test
    void testFindByIdNotFound() {
        repo.add(new TestItem("T001", "Item Satu"));
        assertNull(repo.findById("T999"));
    }

    @Test
    void testFindByIdNullSafety() {
        repo.add(new TestItem(null, "Tanpa ID"));
        assertNull(repo.findById("T001")); // no NPE
    }

    @Test
    void testFind() {
        repo.add(new TestItem("T001", "Budi"));
        repo.add(new TestItem("T002", "Ani"));
        repo.add(new TestItem("T003", "Budi"));

        List<TestItem> hasil = repo.find(item -> "Budi".equals(item.getName()));
        assertEquals(2, hasil.size());
    }

    @Test
    void testDelete() {
        repo.add(new TestItem("T001", "Item Satu"));
        repo.add(new TestItem("T002", "Item Dua"));

        assertTrue(repo.delete("T001"));
        assertEquals(1, repo.size());
        assertNull(repo.findById("T001"));
    }

    @Test
    void testDeleteNotFound() {
        repo.add(new TestItem("T001", "Item Satu"));
        assertFalse(repo.delete("T999"));
    }

    @Test
    void testUpdate() {
        repo.add(new TestItem("T001", "Item Lama"));
        assertTrue(repo.update(new TestItem("T001", "Item Baru")));
        assertEquals("Item Baru", repo.findById("T001").getName());
    }

    @Test
    void testUpdateNotFound() {
        repo.add(new TestItem("T001", "Item Satu"));
        assertFalse(repo.update(new TestItem("T999", "Item Baru")));
    }

    @Test
    void testUpdateNullId() {
        assertFalse(repo.update(new TestItem(null, "No ID")));
    }

    @Test
    void testGetAll() {
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T002", "Dua"));

        List<TestItem> all = repo.getAll();
        assertEquals(2, all.size());

        // Harusnya defensive copy
        all.clear();
        assertEquals(2, repo.size()); // original tidak terpengaruh
    }

    @Test
    void testClear() {
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T002", "Dua"));

        repo.clear();
        assertEquals(0, repo.size());
    }

    @Test
    void testSize() {
        assertEquals(0, repo.size());
        repo.add(new TestItem("T001", "Satu"));
        assertEquals(1, repo.size());
    }

    @Test
    void testGenerateId() {
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T002", "Dua"));

        String newId = repo.generateId("T");
        assertEquals("T003", newId);
    }

    @Test
    void testGenerateIdEmpty() {
        String newId = repo.generateId("T");
        assertEquals("T001", newId);
    }

    @Test
    void testGenerateIdWithNonStandardFormat() {
        repo.add(new TestItem("X001", "Beda prefix"));
        repo.add(new TestItem("T999", "Satu"));

        String newId = repo.generateId("T");
        assertEquals("T1000", newId);
    }

    @Test
    void testSaveAndLoadPersistence() throws Exception {
        repo.add(new TestItem("T001", "Item Tersimpan"));
        boolean saved = repo.saveToJson();
        assertTrue(saved);

        // Buat repo baru dengan file yang sama
        Type type = new TypeToken<ArrayList<TestItem>>() {}.getType();
        Repository<TestItem> repo2 = new Repository<>(tempFile, type);

        assertEquals(1, repo2.size());
        assertNotNull(repo2.findById("T001"));
        assertEquals("Item Tersimpan", repo2.findById("T001").getName());
    }

    @Test
    void testLoadFromNonexistentFile() {
        String fakePath = "/tmp/opencode/tidak_ada_" + System.currentTimeMillis() + ".json";
        Type type = new TypeToken<ArrayList<TestItem>>() {}.getType();
        Repository<TestItem> emptyRepo = new Repository<>(fakePath, type);
        assertEquals(0, emptyRepo.size());
    }

    // ========== getSorted TESTS ==========

    @Test
    void testGetSortedById() {
        repo.add(new TestItem("T003", "Tiga"));
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T002", "Dua"));

        List<TestItem> sorted = repo.getSorted(
            Comparator.comparing(TestItem::getId));

        assertEquals("T001", sorted.get(0).getId());
        assertEquals("T002", sorted.get(1).getId());
        assertEquals("T003", sorted.get(2).getId());
        // Data internal tidak berubah
        assertEquals(3, repo.size());
        assertEquals("T003", repo.findById("T003").getId());
    }

    @Test
    void testGetSortedByName() {
        repo.add(new TestItem("T001", "Charlie"));
        repo.add(new TestItem("T002", "Alpha"));
        repo.add(new TestItem("T003", "Bravo"));

        List<TestItem> sorted = repo.getSorted(
            Comparator.comparing(TestItem::getName));

        assertEquals("Alpha", sorted.get(0).getName());
        assertEquals("Bravo", sorted.get(1).getName());
        assertEquals("Charlie", sorted.get(2).getName());
    }

    @Test
    void testGetSortedEmptyRepo() {
        List<TestItem> sorted = repo.getSorted(
            Comparator.comparing(TestItem::getId));
        assertTrue(sorted.isEmpty());
    }

    @Test
    void testGetSortedDoesNotMutateInternal() {
        repo.add(new TestItem("T003", "Tiga"));
        repo.add(new TestItem("T001", "Satu"));

        List<TestItem> sorted = repo.getSorted(
            Comparator.comparing(TestItem::getId));
        assertEquals("T001", sorted.get(0).getId());

        // Internal list tetap dalam urutan original (insertion order)
        assertEquals("T003", repo.getAll().get(0).getId());
    }

    // ========== findByIdBinary TESTS ==========

    @Test
    void testFindByIdBinaryFound() {
        repo.add(new TestItem("T003", "Tiga"));
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T002", "Dua"));

        TestItem found = repo.findByIdBinary("T002");
        assertNotNull(found);
        assertEquals("Dua", found.getName());
    }

    @Test
    void testFindByIdBinaryFoundFirstElement() {
        repo.add(new TestItem("T003", "Tiga"));
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T002", "Dua"));

        TestItem found = repo.findByIdBinary("T001");
        assertNotNull(found);
        assertEquals("Satu", found.getName());
    }

    @Test
    void testFindByIdBinaryFoundLastElement() {
        repo.add(new TestItem("T003", "Tiga"));
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T002", "Dua"));

        TestItem found = repo.findByIdBinary("T003");
        assertNotNull(found);
        assertEquals("Tiga", found.getName());
    }

    @Test
    void testFindByIdBinaryNotFound() {
        repo.add(new TestItem("T001", "Satu"));
        repo.add(new TestItem("T003", "Tiga"));

        assertNull(repo.findByIdBinary("T999"));
    }

    @Test
    void testFindByIdBinaryEmptyRepo() {
        assertNull(repo.findByIdBinary("T001"));
    }

    @Test
    void testFindByIdBinaryNullInput() {
        repo.add(new TestItem("T001", "Satu"));
        assertNull(repo.findByIdBinary(null));
    }
}
