package collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.time.LocalDate;

import interfaces.Identifiable;
import model.Kategori;
import model.base.ItemPerpustakaan;
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
 * Menerapkan konsep Generic dengan bounded type parameter Identifiable.
 *
 * @param <T> tipe objek yang dikelola (harus implements Identifiable)
 */
public class Repository<T extends Identifiable> {

    private ArrayList<T> items;
    private String filePath;
    private Type typeToken;

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
    public T findById(String id) {
        for (T item : items) {
            if (item.getId() != null && item.getId().equals(id)) {
                return item;
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
    public boolean delete(String id) {
        return items.removeIf(item -> item.getId() != null && item.getId().equals(id));
    }

    /**
     * Memperbarui item (replace item lama dengan yang baru berdasarkan ID).
     * @param updatedItem item dengan data terbaru
     * @return true jika berhasil diupdate
     */
    public boolean update(T updatedItem) {
        String targetId = updatedItem.getId();
        if (targetId == null) return false;

        for (int i = 0; i < items.size(); i++) {
            String currentId = items.get(i).getId();
            if (currentId != null && currentId.equals(targetId)) {
                items.set(i, updatedItem);
                return true;
            }
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
    public boolean saveToJson() {
        Gson gson = createGson().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(items, writer);
            return true;
        } catch (IOException e) {
            System.err.println("Gagal menyimpan ke " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    /** Memuat data dari file JSON */
    public void loadFromJson() {
        File file = new File(filePath);
        if (!file.exists()) {
            items = new ArrayList<>();
            return;
        }

        com.google.gson.Gson gson = createGson().create();
        try (Reader reader = new FileReader(file)) {
            items = gson.fromJson(reader, typeToken);
            if (items == null) {
                items = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat dari " + filePath + ": " + e.getMessage());
            items = new ArrayList<>();
        } catch (com.google.gson.JsonParseException e) {
            System.err.println("Format JSON tidak valid di " + filePath
                + ". Data akan di-reset. (" + e.getMessage() + ")");
            items = new ArrayList<>();
        }
    }

    /**
     * Membangun Gson instance dengan RuntimeTypeAdapterFactory
     * untuk mendukung polymorphic deserialization ItemPerpustakaan.
     */
    private GsonBuilder createGson() {
        RuntimeTypeAdapterFactory<ItemPerpustakaan> typeFactory =
            RuntimeTypeAdapterFactory.of(ItemPerpustakaan.class, "type")
                .registerSubtype(BukuFisik.class, "Buku Fisik")
                .registerSubtype(BukuDigital.class, "Buku Digital")
                .registerSubtype(Jurnal.class, "Jurnal");

        // TypeAdapter untuk Kategori enum — handle backward compat
        JsonDeserializer<Kategori> kategoriDeserializer = (json, type, ctx) -> {
            String value = json.getAsString();
            return Kategori.fromString(value);
        };

        // TypeAdapter untuk LocalDate — bypass Java 17+ reflection restrictions
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
            String itemId = item.getId();
            if (itemId != null && itemId.startsWith(prefix)) {
                String numStr = itemId.substring(prefix.length());
                try {
                    int num = Integer.parseInt(numStr);
                    maxNum = Math.max(maxNum, num);
                } catch (NumberFormatException e) {
                    // skip item dengan format ID berbeda
                }
            }
        }
        return prefix + String.format("%03d", maxNum + 1);
    }
}
