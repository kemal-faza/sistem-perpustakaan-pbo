package exception;

/**
 * Exception thrown ketika anggota sudah mencapai batas maksimal peminjaman.
 */
public class PeminjamanMelebihiBatasException extends Exception {
    private static final long serialVersionUID = 1L;

    public PeminjamanMelebihiBatasException(String message) {
        super(message);
    }
}
