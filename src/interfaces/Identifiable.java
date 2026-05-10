package interfaces;

/**
 * Interface untuk objek yang memiliki identitas unik (ID).
 * Digunakan sebagai bound generic di Repository agar type-safe.
 */
public interface Identifiable {
    String getId();
}
