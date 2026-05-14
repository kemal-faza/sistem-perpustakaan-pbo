package service;

import collection.Repository;
import exception.BukuTidakDitemukanException;
import interfaces.ISearchable;
import model.AddResult;
import model.Anggota;
import model.base.ItemPerpustakaan;

import java.util.ArrayList;
import java.util.List;

public class BukuService {
    private Repository<ItemPerpustakaan> repoBuku;
    private Repository<Anggota> repoAnggota;

    public BukuService(Repository<ItemPerpustakaan> repoBuku, Repository<Anggota> repoAnggota) {
        this.repoBuku = repoBuku;
        this.repoAnggota = repoAnggota;
    }

    public AddResult tambahBuku(ItemPerpustakaan item) {
        String itemPenulis = (item.getPenulis() != null) ? item.getPenulis() : "";
        for (ItemPerpustakaan existing : repoBuku.getAll()) {
            String existPenulis = (existing.getPenulis() != null) ? existing.getPenulis() : "";
            if (existing.getJudul().equalsIgnoreCase(item.getJudul())
                    && existing.getTipe().equals(item.getTipe())
                    && existPenulis.equalsIgnoreCase(itemPenulis)) {
                existing.setStok(existing.getStok() + item.getStok());
                return AddResult.STOK;
            }
        }
        repoBuku.add(item);
        return AddResult.BARU;
    }

    public void hapusBuku(String idBuku) throws BukuTidakDitemukanException {
        ItemPerpustakaan item = repoBuku.findById(idBuku);
        if (item == null) {
            throw new BukuTidakDitemukanException(
                    "Item dengan ID '" + idBuku + "' tidak ditemukan.");
        }
        repoBuku.delete(idBuku);
    }

    public List<ItemPerpustakaan> cariBuku(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repoBuku.getAll();
        }

        List<ItemPerpustakaan> allBooks = repoBuku.getAll();
        List<ItemPerpustakaan> result = new ArrayList<>();

        for (ItemPerpustakaan item : allBooks) {
            if (item instanceof ISearchable && ((ISearchable) item).cocok(keyword)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<ItemPerpustakaan> getAllBuku() {
        return repoBuku.getAll();
    }

    public ItemPerpustakaan getBukuById(String idBuku) {
        return repoBuku.findById(idBuku);
    }

    public void tambahAnggota(Anggota anggota) {
        repoAnggota.add(anggota);
    }

    public List<Anggota> getAllAnggota() {
        return repoAnggota.getAll();
    }

    public Anggota getAnggotaById(String idAnggota) {
        return repoAnggota.findById(idAnggota);
    }

    public String generateIdBuku() {
        return repoBuku.generateId("B");
    }

    public String generateIdAnggota() {
        return repoAnggota.generateId("A");
    }
}
