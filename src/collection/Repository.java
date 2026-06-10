package collection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import interfaces.Identifiable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final Gson gson;
    private final Gson prettyGson;

    /**
     * Constructor repository dengan Gson dari GsonFactory.
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
     * Mencari item berdasarkan ID menggunakan algoritma Binary Search — O(log n).
     * Data diurutkan by ID terlebih dahulu (jika belum sorted),
     * kemudian dilakukan pencarian dengan membagi ruang pencarian menjadi
     * setengah di setiap iterasi.
     *
     * @param id ID item yang dicari
     * @return item jika ditemukan, null jika tidak
     */
    public T findByIdBinary(String id) {
        if (id == null || items.isEmpty()) return null;

        // Sort internal list by ID (ascending) untuk memastikan prasyarat binary search
        items.sort(Comparator.comparing(Identifiable::getId,
                Comparator.nullsLast(Comparator.naturalOrder())));

        int left = 0;
        int right = items.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            String midId = items.get(mid).getId();

            if (midId == null) {
                left = mid + 1;
                continue;
            }

            int cmp = midId.compareTo(id);
            if (cmp == 0) {
                return items.get(mid);
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
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

    /**
     * Mengembalikan salinan list yang sudah diurutkan berdasarkan comparator.
     * Menerapkan algoritma Sorting (TimSort via Collections.sort) — O(n log n).
     * Data internal tidak dimodifikasi.
     *
     * @param comparator comparator untuk menentukan urutan
     * @return list baru yang sudah terurut
     */
    public List<T> getSorted(Comparator<T> comparator) {
        List<T> sorted = new ArrayList<>(items);
        sorted.sort(comparator);
        return sorted;
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
        try (Writer writer = new FileWriter(filePath)) {
            prettyGson.toJson(items, writer);
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
