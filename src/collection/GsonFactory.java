package collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import model.Kategori;
import model.base.ItemPerpustakaan;
import model.BukuFisik;
import model.BukuDigital;
import model.Jurnal;

import java.time.LocalDate;

/**
 * Factory untuk membangun Gson instance yang dikonfigurasi untuk
 * polymorphic deserialization ItemPerpustakaan dan adapter LocalDate.
 *
 * Memisahkan konfigurasi serialisasi dari Repository agar Repository
 * tetap benar-benar generic dan tidak mengetahui concrete subtypes.
 */
public class GsonFactory {

    private GsonFactory() {
        // Utility class — tidak perlu di-instantiate
    }

    /**
     * Membuat GsonBuilder dengan konfigurasi lengkap:
     * - RuntimeTypeAdapterFactory untuk polymorphic ItemPerpustakaan
     * - TypeAdapter untuk Kategori enum (backward compatibility)
     * - TypeAdapter untuk LocalDate (bypass Java 17+ reflection restrictions)
     *
     * @return GsonBuilder yang sudah dikonfigurasi (caller bisa chain .create())
     */
    public static GsonBuilder builder() {
        RuntimeTypeAdapterFactory<ItemPerpustakaan> typeFactory =
            RuntimeTypeAdapterFactory.of(ItemPerpustakaan.class, "type")
                .registerSubtype(BukuFisik.class, "Buku Fisik")
                .registerSubtype(BukuDigital.class, "Buku Digital")
                .registerSubtype(Jurnal.class, "Jurnal");

        JsonDeserializer<Kategori> kategoriDeserializer = (json, type, ctx) -> {
            String value = json.getAsString();
            return Kategori.fromString(value);
        };

        JsonSerializer<LocalDate> localDateSerializer =
            (src, type, ctx) -> new JsonPrimitive(src.toString());
        JsonDeserializer<LocalDate> localDateDeserializer =
            (json, type, ctx) -> LocalDate.parse(json.getAsString());

        return new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .registerTypeAdapter(Kategori.class, kategoriDeserializer)
            .registerTypeAdapter(LocalDate.class, localDateSerializer)
            .registerTypeAdapter(LocalDate.class, localDateDeserializer);
    }

    /**
     * Membuat Gson instance default (compact).
     * @return Gson instance siap pakai
     */
    public static Gson create() {
        return builder().create();
    }

    /**
     * Membuat Gson instance dengan pretty printing.
     * @return Gson instance siap pakai (formatted)
     */
    public static Gson createPretty() {
        return builder().setPrettyPrinting().create();
    }
}
