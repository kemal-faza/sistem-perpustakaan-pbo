package interfaces;

/**
 * Interface untuk objek yang dapat dicari.
 * Mendefinisikan kontrak pencarian berdasarkan keyword.
 */
public interface ISearchable {
    boolean cocok(String keyword);
}
