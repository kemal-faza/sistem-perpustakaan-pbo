# AGENTS.md — Perpustakaan PBO

## Tech Stack
- Java 17+
- Gson 2.10.1 + Gson Extras (RuntimeTypeAdapterFactory)
- CLI (terminal-based, no GUI)
- JSON file persistence

## Build & Run (Linux/macOS)
```bash
# Compile
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java

# Run
java -cp "out:lib/*" Main
```

## Build & Run (Windows CMD)
```batch
REM Compile
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src\Main.java

REM Run
java -cp "out;lib/*" Main
```

## Test (Linux/macOS)
Requires JUnit standalone jar. Run `scripts/test-download.sh` first.
```bash
# Compile tests
javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath src:test test/unit/service/*.java test/unit/collection/*.java test/unit/model/*.java

# Run tests
java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
```

## Test (Windows CMD)
```batch
REM Compile tests
dir /s /B test\*.java > test_sources.txt
javac -encoding UTF-8 -cp "lib/*;out" -d out -sourcepath "src;test" @test_sources.txt
del test_sources.txt

REM Run tests
java -jar lib\junit-platform-console-standalone-1.10.1.jar --cp "out;lib/*" --scan-class-path
```

## Project Structure
```
src/
├── Main.java              — Entry point
├── model/                 — Domain classes
│   ├── abstract/
│   │   └── ItemPerpustakaan.java
│   ├── BukuFisik.java
│   ├── BukuDigital.java
│   ├── Jurnal.java
│   ├── Anggota.java
│   ├── Admin.java
│   ├── Peminjaman.java
│   ├── AddResult.java          — Enum hasil tambah buku
│   └── StatusPeminjaman.java
├── interfaces/
│   ├── Identifiable.java
│   ├── IBorrowable.java
│   ├── ISearchable.java
│   └── ILibraryService.java
├── collection/
│   └── Repository.java
├── exception/
│   ├── BukuTidakTersediaException.java
│   ├── AnggotaTidakValidException.java
│   ├── PeminjamanMelebihiBatasException.java
│   ├── BukuTidakDitemukanException.java
│   └── PeminjamanTidakDitemukanException.java
├── service/
│   ├── PerpustakaanService.java — Facade utama
│   ├── BukuService.java        — CRUD buku & anggota
│   ├── PeminjamanService.java  — Pinjam/kembalikan/denda
│   └── AuthService.java        — Autentikasi
├── ui/
│   ├── MenuManager.java
│   ├── MenuAdmin.java
│   └── MenuAnggota.java
└── util/
    └── Config.java

scripts/
├── build.bat / build.sh        — Compile
├── test.bat / test.sh          — Test
└── test-download.sh            — Download JUnit

test/
└── unit/
    ├── model/                  — 7 test classes
    ├── collection/             — RepositoryTest
    └── service/                — AuthServiceTest, PerpustakaanServiceTest

data/
├── buku.json
├── anggota.json
└── peminjaman.json
```


## Key Architectural Decisions

| Decision | Reason |
|----------|--------|
| `Repository<T extends Identifiable>` | Type-safe, no reflection. Bounded generic dijamin compile-time |
| Peminjaman stores `String` ID (not object ref) | Avoids circular reference in JSON serialization |
| PerpustakaanService (Singleton) | Single point of orchestration, prevents duplicate instances |
| Model methods throw, not println | Separation of concerns: model enforces invariants, UI handles display |
| 4 custom checked exceptions | Semantic error types for each failure scenario |

## OOP Concepts Covered (~30/32)
- [x] Class & Object, Constructor (default + overloading)
- [x] Encapsulation (private fields + getter/setter)
- [x] Inheritance, Abstract Class, Abstract Method
- [x] Interface, Implements Multiple Interface
- [x] Method Overriding, Method Overloading
- [x] Polymorphism (Inclusion via List<Item>)
- [x] Generic Class, Bounded Generic, Generic Method
- [x] Collection (ArrayList, List, Stream API)
- [x] Exception Handling & Custom Exception
- [x] Association, Composition, Aggregation, Dependency
- [x] super, this, final, static
- [x] Singleton Pattern, Enum, Persistent Object
- [x] Dependency (UI → Service)
- [x] Message Passing (Main → Service → Repository)

## References
- Course materials: `/reference/` (10 PDF files on OOP concepts)
