package exception;

/**
 * Exception thrown ketika buku dengan ID tertentu tidak ditemukan.
 */
public class BukuTidakDitemukanException extends Exception {
    private static final long serialVersionUID = 1L;

    public BukuTidakDitemukanException(String message) {
        super(message);
    }
}
