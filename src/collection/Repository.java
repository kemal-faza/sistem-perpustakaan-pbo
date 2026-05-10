package collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import model.abstracts.ItemPerpustakaan;
import model.BukuFisik;
import model.BukuDigital;
import model.Jurnal;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic class untuk operasi CRUD dan persistensi JSON.
 * Menerapkan konsep Generic dengan bounded type parameter.
 *
 * @param <T> tipe objek yang dikelola
 */
public class Repository<T> {

    private ArrayList<T> items;
    private String filePath;
    private Type typeToken;
    private Class<T> clazz;

    /**
     * Constructor repository.
     * @param filePath path file JSON untuk persistensi
     * @param typeToken TypeToken Gson untuk deserialisasi
     */
    public Repository(String filePath, Type typeToken) {
        this.items = new ArrayList<>();
        this.filePath = filePath;
        this.typeToken = typeToken;
        loadFromJson();
    }

    // === CRUD Operations ===

    /** Menambahkan item ke repository */
    public void add(T item) {
        items.add(item);
    }

    /**
     * Mencari item berdasarkan ID.
     * @param id ID item yang dicari
     * @return item jika ditemukan, null jika tidak
     */
    @SuppressWarnings("unchecked")
    public T findById(String id) {
        for (T item : items) {
            // Gunakan refleksi untuk akses method getId
            try {
                java.lang.reflect.Method getIdMethod = item.getClass().getMethod("getId");
                String itemId = (String) getIdMethod.invoke(item);
                if (itemId != null && itemId.equals(id)) {
                    return item;
                }
            } catch (Exception e) {
                // Jika tidak punya method getId, skip
            }
        }
        return null;
    }

    /**
     * Mencari item dengan kondisi tertentu.
     * @param predicate kondisi filter
     * @return list item yang cocok
     */
    public List<T> find(Predicate<T> predicate) {
        return items.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Menghapus item berdasarkan ID.
     * @param id ID item yang akan dihapus
     * @return true jika berhasil dihapus
     */
    @SuppressWarnings("unchecked")
    public boolean delete(String id) {
        return items.removeIf(item -> {
            try {
                java.lang.reflect.Method getIdMethod = item.getClass().getMethod("getId");
                String itemId = (String) getIdMethod.invoke(item);
                return itemId != null && itemId.equals(id);
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Memperbarui item (replace item lama dengan yang baru berdasarkan ID).
     * @param updatedItem item dengan data terbaru
     * @return true jika berhasil diupdate
     */
    @SuppressWarnings("unchecked")
    public boolean update(T updatedItem) {
        try {
            java.lang.reflect.Method getIdMethod = updatedItem.getClass().getMethod("getId");
            String targetId = (String) getIdMethod.invoke(updatedItem);

            for (int i = 0; i < items.size(); i++) {
                String currentId = (String) getIdMethod.invoke(items.get(i));
                if (currentId != null && currentId.equals(targetId)) {
                    items.set(i, updatedItem);
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /** Mengambil semua item */
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    /** Mengosongkan semua item */
    public void clear() {
        items.clear();
    }

    /** Mendapatkan jumlah item */
    public int size() {
        return items.size();
    }

    // === Persistence (JSON) ===

    /** Menyimpan data ke file JSON */
    public void saveToJson() {
        Gson gson = createGson().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            System.err.println("Gagal menyimpan ke " + filePath + ": " + e.getMessage());
        }
    }

    /** Memuat data dari file JSON */
    public void loadFromJson() {
        File file = new File(filePath);
        if (!file.exists()) {
            items = new ArrayList<>();
            return;
        }

        Gson gson = createGson().create();
        try (Reader reader = new FileReader(file)) {
            items = gson.fromJson(reader, typeToken);
            if (items == null) {
                items = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat dari " + filePath + ": " + e.getMessage());
            items = new ArrayList<>();
        }
    }

    /**
     * Membangun Gson instance dengan RuntimeTypeAdapterFactory
     * untuk mendukung polymorphic deserialization ItemPerpustakaan.
     */
    private GsonBuilder createGson() {
        // Type adapter factory untuk polymorphic ItemPerpustakaan
        RuntimeTypeAdapterFactory<ItemPerpustakaan> typeFactory =
            RuntimeTypeAdapterFactory.of(ItemPerpustakaan.class, "type")
                .registerSubtype(BukuFisik.class, "BukuFisik")
                .registerSubtype(BukuDigital.class, "BukuDigital")
                .registerSubtype(Jurnal.class, "Jurnal");

        GsonBuilder builder = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory);

        return builder;
    }

    // === Utility ===

    /**
     * Men-generate ID unik berdasarkan prefix.
     * Format: [prefix]XXX (contoh: B001, A002, P003)
     * @param prefix prefix ID (B=item, A=anggota, P=peminjaman)
     * @return ID unik
     */
    public String generateId(String prefix) {
        int maxNum = 0;
        for (T item : items) {
            try {
                java.lang.reflect.Method getIdMethod = item.getClass().getMethod("getId");
                String itemId = (String) getIdMethod.invoke(item);
                if (itemId != null && itemId.startsWith(prefix)) {
                    String numStr = itemId.substring(prefix.length());
                    int num = Integer.parseInt(numStr);
                    maxNum = Math.max(maxNum, num);
                }
            } catch (Exception e) {
                // skip
            }
        }
        return prefix + String.format("%03d", maxNum + 1);
    }
}
