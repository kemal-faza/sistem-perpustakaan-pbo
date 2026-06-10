package unit.collection;

import collection.GsonFactory;
import com.google.gson.Gson;
import model.*;
import model.base.ItemPerpustakaan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GsonFactoryTest {

    private final Gson gson = GsonFactory.create();

    @Test
    void testBukuFisikTypeDiscriminator() {
        BukuFisik buku = new BukuFisik("B001", "Test Buku", 2024,
                Kategori.TEKNOLOGI, "Penerbit", "Penulis",
                300, "A1-01", 2);
        // Model sets type discriminator correctly via constructor
        assertEquals("Buku Fisik", buku.getType());
        assertEquals("Buku Fisik", buku.getTipe());

        // Verify GsonFactory can deserialize the same format that production
        // serialization produces (reflective adapter via Collection adapter)
        String json = "{\"type\":\"Buku Fisik\",\"id\":\"B001\",\"judul\":\"Test Buku\","
            + "\"tahunTerbit\":2024,\"kategori\":\"TEKNOLOGI\",\"penerbit\":\"Penerbit\","
            + "\"penulis\":\"Penulis\",\"stok\":2,\"dipinjam\":0,"
            + "\"jumlahHalaman\":300,\"lokasiRak\":\"A1-01\"}";
        ItemPerpustakaan item = gson.fromJson(json, ItemPerpustakaan.class);
        assertInstanceOf(BukuFisik.class, item);
        assertEquals("Test Buku", item.getJudul());
        assertEquals("Buku Fisik", item.getTipe());
    }

    @Test
    void testBukuDigitalTypeDiscriminator() {
        BukuDigital buku = new BukuDigital("B002", "E-Book", 2024,
                Kategori.TEKNOLOGI, "E-Pub", "Penulis",
                5.2, "PDF");
        assertEquals("Buku Digital", buku.getType());
        assertEquals("Buku Digital", buku.getTipe());

        String json = "{\"type\":\"Buku Digital\",\"id\":\"B002\",\"judul\":\"E-Book\","
            + "\"tahunTerbit\":2024,\"kategori\":\"TEKNOLOGI\",\"penerbit\":\"E-Pub\","
            + "\"penulis\":\"Penulis\",\"stok\":1,\"dipinjam\":0,"
            + "\"ukuranFile\":5.2,\"format\":\"PDF\"}";
        ItemPerpustakaan item = gson.fromJson(json, ItemPerpustakaan.class);
        assertInstanceOf(BukuDigital.class, item);
        assertEquals("E-Book", item.getJudul());
        assertEquals("Buku Digital", item.getTipe());
    }

    @Test
    void testJurnalTypeDiscriminator() {
        Jurnal jurnal = new Jurnal("B003", "Jurnal Test", 2024,
                Kategori.ILMIAH, "Universitas", "Editor",
                1, 1, "Ilmu Komputer", 1);
        assertEquals("Jurnal", jurnal.getType());
        assertEquals("Jurnal", jurnal.getTipe());

        String json = "{\"type\":\"Jurnal\",\"id\":\"B003\",\"judul\":\"Jurnal Test\","
            + "\"tahunTerbit\":2024,\"kategori\":\"ILMIAH\",\"penerbit\":\"Universitas\","
            + "\"penulis\":\"Editor\",\"stok\":1,\"dipinjam\":0,"
            + "\"volume\":1,\"nomor\":1,\"bidang\":\"Ilmu Komputer\"}";
        ItemPerpustakaan item = gson.fromJson(json, ItemPerpustakaan.class);
        assertInstanceOf(Jurnal.class, item);
        assertEquals("Jurnal Test", item.getJudul());
        assertEquals("Jurnal", item.getTipe());
    }

    @Test
    void testDeserializePolymorphic() {
        String json = """
            [
              {"type":"Buku Fisik","id":"B001","judul":"Test","tahunTerbit":2024,
               "kategori":"TEKNOLOGI","penerbit":"Pub","penulis":"Pen",
               "stok":1,"dipinjam":0,"jumlahHalaman":100,"lokasiRak":"A1"},
              {"type":"Buku Digital","id":"B002","judul":"E-Book","tahunTerbit":2024,
               "kategori":"TEKNOLOGI","penerbit":"Pub","penulis":"Pen",
               "stok":1,"dipinjam":0,"ukuranFile":3.5,"format":"PDF"},
              {"type":"Jurnal","id":"B003","judul":"Jurnal","tahunTerbit":2024,
               "kategori":"ILMIAH","penerbit":"Univ","penulis":"Ed",
               "stok":1,"dipinjam":0,"volume":1,"nomor":1,"bidang":"CS"}
            ]
            """;

        var type = new com.google.gson.reflect.TypeToken<ArrayList<ItemPerpustakaan>>() {}.getType();
        List<ItemPerpustakaan> items = gson.fromJson(json, type);

        assertEquals(3, items.size());
        assertInstanceOf(BukuFisik.class, items.get(0));
        assertInstanceOf(BukuDigital.class, items.get(1));
        assertInstanceOf(Jurnal.class, items.get(2));
    }

    @Test
    void testLocalDateSerialization() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        String json = gson.toJson(date);
        assertEquals("\"2024-06-15\"", json);
    }

    @Test
    void testLocalDateDeserialization() {
        LocalDate date = gson.fromJson("\"2024-06-15\"", LocalDate.class);
        assertEquals(LocalDate.of(2024, 6, 15), date);
    }

    @Test
    void testKategoriBackwardCompat() {
        String json = """
            {"type":"Buku Fisik","id":"B001","judul":"Test","tahunTerbit":2024,
             "kategori":"TEKNOLOGI","penerbit":"Pub","penulis":"Pen",
             "stok":1,"dipinjam":0,"jumlahHalaman":100,"lokasiRak":"A1"}
            """;
        ItemPerpustakaan item = gson.fromJson(json, ItemPerpustakaan.class);
        assertEquals(Kategori.TEKNOLOGI, item.getKategori());
    }
}
