package interfaces;

/**
 * Interface untuk objek yang dapat dipinjam.
 * Mendefinisikan kontrak perilaku peminjaman.
 */
public interface IBorrowable {
    void pinjam();
    void kembalikan();
    void perpanjang();
}
