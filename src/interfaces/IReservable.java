package interfaces;

import model.Anggota;

/**
 * Interface untuk objek yang dapat di-reservasi.
 * Mendefinisikan kontrak reservasi oleh anggota.
 */
public interface IReservable {
    void reservasi(Anggota a);
    void batalReservasi();
}
