package exception;

/**
 * Exception thrown ketika buku dengan ID tertentu tidak ditemukan.
 */
public class BukuTidakDitemukanException extends Exception {
    public BukuTidakDitemukanException(String message) {
        super(message);
    }
}
