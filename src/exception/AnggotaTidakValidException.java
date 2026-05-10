package exception;

/**
 * Exception thrown ketika anggota tidak valid atau tidak ditemukan.
 */
public class AnggotaTidakValidException extends Exception {
    public AnggotaTidakValidException(String message) {
        super(message);
    }
}
