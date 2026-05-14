package exception;

/**
 * Exception thrown ketika peminjaman dengan ID tertentu tidak ditemukan.
 */
public class PeminjamanTidakDitemukanException extends Exception {
    private static final long serialVersionUID = 1L;

    public PeminjamanTidakDitemukanException(String message) {
        super(message);
    }
}
