package exception;

/**
 * Exception thrown ketika buku yang ingin dipinjam sedang tidak tersedia.
 */
public class BukuTidakTersediaException extends Exception {
    private static final long serialVersionUID = 1L;

    public BukuTidakTersediaException(String message) {
        super(message);
    }
}
