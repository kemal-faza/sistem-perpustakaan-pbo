package exception;

/**
 * Exception thrown ketika anggota tidak valid atau tidak ditemukan.
 */
public class AnggotaTidakValidException extends Exception {
    private static final long serialVersionUID = 1L;

    public AnggotaTidakValidException(String message) {
        super(message);
    }
}
