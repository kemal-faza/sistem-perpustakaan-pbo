package model;

/**
 * Enum untuk kategori item perpustakaan.
 * Memberikan pilihan kategori yang tetap (tidak bisa diisi sembarangan).
 */
public enum Kategori {
    TEKNOLOGI("Teknologi"),
    ILMIAH("Ilmiah"),
    FIKSI("Fiksi"),
    NON_FIKSI("Non-Fiksi"),
    SEJARAH("Sejarah"),
    PENDIDIKAN("Pendidikan"),
    REFERENSI("Referensi"),
    UMUM("Umum");

    private final String displayName;

    Kategori(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Konversi dari string (display name atau constant name) ke Kategori.
     * Untuk backward compatibility dengan data lama.
     */
    public static Kategori fromString(String s) {
        if (s == null) return UMUM;
        for (Kategori k : values()) {
            if (k.displayName.equalsIgnoreCase(s.trim())
                || k.name().equalsIgnoreCase(s.trim())) {
                return k;
            }
        }
        return UMUM;
    }
}
