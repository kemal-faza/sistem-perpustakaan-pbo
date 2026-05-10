package exception;

/**
 * Exception thrown ketika anggota sudah mencapai batas maksimal peminjaman.
 */
public class PeminjamanMelebihiBatasException extends Exception {
    public PeminjamanMelebihiBatasException(String message) {
        super(message);
    }
}
