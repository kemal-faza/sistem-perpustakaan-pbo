package model;

/**
 * Enum yang merepresentasikan status peminjaman.
 * - DIPINJAM: item sedang dipinjam
 * - DIKEMBALIKAN: item sudah dikembalikan tepat waktu
 * - TERLAMBAT: item dikembalikan setelah batas waktu
 */
public enum StatusPeminjaman {
    DIPINJAM,
    DIKEMBALIKAN,
    TERLAMBAT
}
