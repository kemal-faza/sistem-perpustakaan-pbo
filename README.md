# Sistem Manajemen Perpustakaan

> **Tugas Besar Pemrograman Berorientasi Objek** — Universitas Diponegoro

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Tests](https://img.shields.io/badge/Tests-108%20passed-green)
![CLI](https://img.shields.io/badge/Platform-CLI-blue)

Aplikasi CLI berbasis Java untuk manajemen perpustakaan: kelola buku, anggota, peminjaman, dan denda otomatis. Data tersimpan dalam format JSON dengan bantuan library Gson.

---

## Daftar Isi

- [Fitur](#fitur)
- [Panduan Cepat](#panduan-cepat)
- [Format Input Data](#format-input-data)
- [UML Class Diagram](#uml-class-diagram)
- [Struktur Project](#struktur-project)
- [Konsep OOP yang Diterapkan](#konsep-oop-yang-diterapkan)
- [Dependencies](#dependencies)
- [Author](#author)

---

## Fitur

| Fitur                              | Admin | Anggota     |
| ---------------------------------- | :---: | :---------: |
| Login                              |  ✅   | ✅ (via ID) |
| Tambah Buku (Fisik/Digital/Jurnal) |  ✅   |             |
| Edit / Hapus Buku                  |  ✅   |             |
| Lihat Semua Buku                   |  ✅   |             |
| Cari Buku                          |  ✅   |     ✅      |
| Manajemen Anggota                  |  ✅   |             |
| Laporan Peminjaman + Denda         |  ✅   |             |
| Pinjam Buku                        |       |     ✅      |
| Kembalikan Buku (auto denda)       |       |     ✅      |
| Riwayat Peminjaman                 |       |     ✅      |
| Perpanjang Peminjaman              |       |     ✅      |
| Data Persistent (JSON)             |  ✅   |     ✅      |

---

## Panduan Cepat

### Prerequisites

- Java JDK 17+

| Langkah              | Linux / macOS                                    | Windows CMD                                      |
| -------------------- | ------------------------------------------------ | ------------------------------------------------ |
| **Compile**          | `javac -cp "lib/*" -d out -sourcepath src src/Main.java` | `javac -cp "lib/*" -d out -sourcepath src src\Main.java` |
| **Run**              | `java -cp "out:lib/*" Main`                     | `java -cp "out;lib/*" Main`                      |
| **Compile + Run**    | `chmod +x scripts/build.sh && ./scripts/build.sh` | `scripts\build.bat`                              |
| **Compile Tests**    | `javac -cp "lib/*:out" -d out -sourcepath src:test test/unit/service/*.java test/unit/collection/*.java test/unit/model/*.java` | `dir /s /B test\*.java > test_sources.txt`<br>`javac -cp "lib/*;out" -d out -sourcepath "src;test" @test_sources.txt`<br>`del test_sources.txt` |
| **Run Tests**        | `java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path` | `java -jar lib\junit-platform-console-standalone-1.10.1.jar --cp "out;lib/*" --scan-class-path` |

> **Catatan:** Test dependencies (JUnit 5 + Mockito) perlu di-download terlebih dahulu. Jalankan `scripts/test-download.sh` (Linux/macOS) atau `scripts\test-download.bat` (Windows) sebelum menjalankan test.

---

## Format Input Data

<details>
<summary>Klik untuk melihat detail format input setiap tipe item</summary>

### Buku Fisik

| Field          | Tipe                  | Wajib? | Contoh                           |
| -------------- | --------------------- | :----: | -------------------------------- |
| Judul          | String                |   ✅   | "Pemrograman Berorientasi Objek" |
| Tahun Terbit   | Angka positif         |   ✅   | 2024                             |
| Kategori       | Pilih dari menu (1-8) |   ✅   | Teknologi, Ilmiah, Fiksi         |
| Penulis        | String                |   ✅   | "Rosa A.S."                      |
| Penerbit       | String                |        | "Informatika"                    |
| Jumlah Halaman | Angka positif         |   ✅   | 350                              |
| Lokasi Rak     | Format `[A]\d+-\d+`   |   ✅   | `A1-01`, `B2-15`                 |
| Jumlah Stok    | Angka positif         |   ✅   | 3                                |

### Buku Digital

| Field        | Tipe               | Wajib? | Contoh                      |
| ------------ | ------------------ | :----: | --------------------------- |
| Judul        | String             |   ✅   | "Belajar Java dalam Sehari" |
| Tahun Terbit | Angka positif      |   ✅   | 2024                        |
| Kategori     | Pilih dari menu    |   ✅   | Teknologi                   |
| Penulis      | String             |   ✅   | "Budi Raharjo"              |
| Penerbit     | String             |        | "E-Book Publisher"          |
| Ukuran File  | Angka desimal (MB) |   ✅   | 5.2                         |
| Format       | String             |   ✅   | PDF                         |

### Jurnal

| Field       | Tipe                                              | Wajib? | Contoh          |
| ----------- | ------------------------------------------------- | :----: | --------------- |
| ...         | (sama: judul, tahun, kategori, penulis, penerbit) |        |                 |
| Volume      | Angka positif                                     |   ✅   | 12              |
| Nomor       | Angka positif                                     |   ✅   | 1               |
| Bidang Ilmu | String                                            |        | "Ilmu Komputer" |
| Jumlah Stok | Angka positif                                     |   ✅   | 2               |

### Format Lokasi Rak

- **Pola:** `[Huruf Lantai][Nomor Rak]-[Slot]`
- Contoh valid: `A1-01`, `B2-15`, `C3-100`
- Contoh tidak valid: `a1` (huruf kecil), `A-1` (tanpa nomor rak), `deket pintu`

</details>

---

## UML Class Diagram

```mermaid
classDiagram
    direction TB

    %% ── ENUM ──
    class StatusPeminjaman {
        <<enumeration>>
        DIPINJAM
        DIKEMBALIKAN
        TERLAMBAT
    }
    class AddResult {
        <<enumeration>>
        BARU
        STOK
    }

    %% ── INTERFACES ──
    class Identifiable {
        <<interface>>
        + getId() String
    }
    class IBorrowable {
        <<interface>>
        + pinjam() void
        + kembalikan() void
        + perpanjang() void
    }
    class ISearchable {
        <<interface>>
        + cocok(String keyword) boolean
    }
    class ILibraryService {
        <<interface>>
        + loginAdmin() boolean
        + pinjamBuku() void
        + getAllBuku() List
        + ...
    }

    %% ── ABSTRACT CLASS ──
    class ItemPerpustakaan {
        <<abstract>>
        - String id
        - String judul
        - int tahunTerbit
        - Kategori kategori
        - String penerbit
        - String penulis
        - int stok
        - int dipinjam
        + hitungDenda(int)* double
        + getTipe()* String
    }

    %% ── CONCRETE CLASSES ──
    class BukuFisik {
        - int jumlahHalaman
        - String lokasiRak
        + hitungDenda(int) double
        + getTipe() String
    }
    class BukuDigital {
        - double ukuranFile
        - String format
        + hitungDenda(int) double
        + getTipe() String
    }
    class Jurnal {
        - int volume
        - int nomor
        - String bidang
        + hitungDenda(int) double
        + getTipe() String
    }

    class Anggota {
        - String id
        - String nama
        - int pinjamanAktif
        + bisaPinjam() boolean
        + tambahPinjaman() void
        + kurangiPinjaman() void
    }
    class Admin {
        - String username
        - String password
        + validatePassword(String) boolean
    }
    class Peminjaman {
        - String idPeminjaman
        - String idAnggota
        - String idItem
        - LocalDate tanggalPinjam
        - LocalDate tanggalKembali
        - LocalDate tanggalDikembalikan
        - StatusPeminjaman status
        - double denda
        + hitungHariTerlambat() int
        + perpanjang() void
        + kembalikan() int
    }

    class Repository~T~ {
        - ArrayList~T~ items
        - String filePath
        + add(T) void
        + findById(String) T
        + find(Predicate) List~T~
        + delete(String) boolean
        + update(T) boolean
        + getAll() List~T~
        + saveToJson() boolean
        + loadFromJson() void
        + generateId(String) String
    }

    class PerpustakaanService {
        - Repository repoBuku
        - Repository repoAnggota
        - Repository repoPeminjaman
        - Admin currentAdmin
        - Anggota currentAnggota
        - AuthService authService
        - BukuService bukuService
        - PeminjamanService peminjamanService
        + getInstance()$ PerpustakaanService
        + loginAdmin() boolean
        + tambahBuku() AddResult
        + pinjamBuku() void
        + kembalikanBuku() Peminjaman
    }
    class BukuService {
        - Repository repoBuku
        - Repository repoAnggota
        + tambahBuku() AddResult
        + cariBuku() List
        + getAllBuku() List
    }
    class PeminjamanService {
        - Repository repoBuku
        - Repository repoAnggota
        - Repository repoPeminjaman
        + pinjamBuku() void
        + kembalikanBuku() Peminjaman
        + perpanjangPeminjaman() void
    }

    class AuthService {
        - Repository repoAnggota
        - Admin admin
        + validateAdmin() boolean
        + loginAnggota() Anggota
    }

    class MenuManager {
        + tampilkanHeader() void
        + tampilkanMenu(String[]) int
        + bacaInput() String
        + tungguEnter() void
    }
    class MenuAdmin
    class MenuAnggota
    class Config {
        + getInstance()$ Config
        + getAdminUsername() String
        + getAdminPassword() String
    }

    %% ── EXCEPTIONS ──
    class BukuTidakTersediaException
    class AnggotaTidakValidException
    class PeminjamanMelebihiBatasException
    class BukuTidakDitemukanException
    class PeminjamanTidakDitemukanException

    %% ── RELATIONSHIPS ──
    ItemPerpustakaan ..|> Identifiable
    ItemPerpustakaan ..|> IBorrowable
    ItemPerpustakaan ..|> ISearchable
    Anggota ..|> Identifiable
    Peminjaman ..|> Identifiable
    Admin ..|> Identifiable

    ItemPerpustakaan <|-- BukuFisik
    ItemPerpustakaan <|-- BukuDigital
    ItemPerpustakaan <|-- Jurnal

    Peminjaman --> Anggota : idAnggota
    Peminjaman --> ItemPerpustakaan : idItem
    Peminjaman --> StatusPeminjaman

    PerpustakaanService o-- BukuService
    PerpustakaanService o-- PeminjamanService
    PerpustakaanService o-- AuthService
    PerpustakaanService --> Repository : mengelola

    BukuService --> Repository
    PeminjamanService --> Repository
    AuthService --> Repository

    Config --> Admin : credential

    MenuAdmin --|> MenuManager
    MenuAnggota --|> MenuManager

    BukuTidakTersediaException --|> Exception
    AnggotaTidakValidException --|> Exception
    PeminjamanMelebihiBatasException --|> Exception
    BukuTidakDitemukanException --|> Exception
    PeminjamanTidakDitemukanException --|> Exception
```

---

## Struktur Project

```
perpustakaan-pbo/
├── src/
│   ├── Main.java              — Entry point
│   ├── model/                 — Domain classes
│   │   ├── abstract/
│   │   │   └── ItemPerpustakaan.java
│   │   ├── BukuFisik.java
│   │   ├── BukuDigital.java
│   │   ├── Jurnal.java
│   │   ├── Anggota.java
│   │   ├── Admin.java
│   │   ├── Peminjaman.java
│   │   ├── AddResult.java          — Enum hasil tambah buku
│   │   └── StatusPeminjaman.java
│   ├── interfaces/
│   │   ├── Identifiable.java
│   │   ├── IBorrowable.java
│   │   ├── ISearchable.java
│   │   └── ILibraryService.java
│   ├── collection/
│   │   └── Repository.java
│   ├── exception/
│   │   ├── BukuTidakTersediaException.java
│   │   ├── AnggotaTidakValidException.java
│   │   ├── PeminjamanMelebihiBatasException.java
│   │   ├── BukuTidakDitemukanException.java
│   │   └── PeminjamanTidakDitemukanException.java
│   ├── service/
│   │   ├── PerpustakaanService.java — Facade utama
│   │   ├── BukuService.java        — CRUD buku & anggota
│   │   ├── PeminjamanService.java  — Pinjam/kembalikan/denda
│   │   └── AuthService.java        — Autentikasi
│   ├── ui/
│   │   ├── MenuManager.java
│   │   ├── MenuAdmin.java
│   │   └── MenuAnggota.java
│   └── util/
│       └── Config.java
│
├── scripts/
│   ├── build.bat / build.sh     — Compile
│   ├── test.bat / test.sh       — Test
│   └── test-download.sh         — Download JUnit
│
├── test/
│   └── unit/
│       ├── model/               — 7 test classes
│       ├── collection/          — RepositoryTest
│       └── service/             — AuthServiceTest, PerpustakaanServiceTest
│
├── data/
│   ├── buku.json
│   ├── anggota.json
│   └── peminjaman.json
├── lib/
│   ├── gson-2.10.1.jar
│   └── gson-extras-2.13.2-rc1.jar
├── config.properties / config.properties.example
├── .gitignore
├── AGENTS.md
├── PLAN.md
└── README.md
```

---

## Login

### Admin

- **Username:** `admin`
- **Password:** lihat `config.properties` (copy dari `config.properties.example` jika belum ada)

### Anggota (Sample Data)

| ID   | Nama         | Pinjaman |
| :--: | ------------ | :------: |
| A001 | Budi Santoso |   0/3    |
| A002 | Siti Rahayu  |   0/3    |
| A003 | Ahmad Fauzi  |   0/3    |

---

## Konsep OOP yang Diterapkan

| #  | Konsep                               | Status |
| -- | ------------------------------------ | :----: |
| 1  | Class & Object                       |   ✅   |
| 2  | Constructor (default + overloading)  |   ✅   |
| 3  | Enkapsulasi (private + getter/setter)|   ✅   |
| 4  | Inheritance (single + hierarchical)  |   ✅   |
| 5  | Abstract Class & Abstract Method     |   ✅   |
| 6  | Interface & Implements Multiple      |   ✅   |
| 7  | Method Overriding & Overloading      |   ✅   |
| 8  | Polymorphism (Inclusion via List)    |   ✅   |
| 9  | Generic Class + Bounded Generic      |   ✅   |
| 10 | Collection (ArrayList, Stream API)   |   ✅   |
| 11 | Exception Handling & Custom Exception|   ✅   |
| 12 | Association, Composition, Aggregation|   ✅   |
| 13 | super, this, final, static           |   ✅   |
| 14 | Singleton Pattern, Enum              |   ✅   |
| 15 | Persistent Object (JSON + Gson)      |   ✅   |
| 16 | Message Passing (Main ke Service)    |   ✅   |
| 17 | Dependency Injection (via interface) |   ✅   |
| 18 | Facade Pattern (PerpustakaanService) |   ✅   |
| 19 | JSON Polymorphic Deserialization     |   ✅   |
| 20 | Thread-Safe Singleton (synchronized) |   ✅   |
| **Total** | **20 konsep**                   | **✅** |

---

## Dependencies

| Library | Versi | Penggunaan |
|---------|-------|------------|
| [Gson](https://github.com/google/gson) | 2.10.1 | Serialisasi / deserialisasi JSON |
| [Gson Extras](https://github.com/google/gson) | 2.13.2-rc1 | Polymorphic type adapter untuk RuntimeTypeAdapterFactory |
| [JUnit 5](https://junit.org/junit5/) | 5.10.1 | Unit testing (108 test cases) |
| [Mockito](https://site.mockito.org/) | 5.7.0 | Test mocking (ByteBuddy + Objenesis) |

---

## Author

- **Nama:** [@kemal](https://github.com/kemal-faza)
- **Mata Kuliah:** Pemrograman Berorientasi Objek
- **Institusi:** Universitas Diponegoro

---

*Project ini dibuat sebagai tugas besar mata kuliah Pemrograman Berorientasi Objek.*
