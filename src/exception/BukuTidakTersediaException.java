package exception;

/**
 * Exception thrown ketika buku yang ingin dipinjam sedang tidak tersedia.
 */
public class BukuTidakTersediaException extends Exception {
    public BukuTidakTersediaException(String message) {
        super(message);
    }
}
