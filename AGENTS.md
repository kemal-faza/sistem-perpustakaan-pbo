# AGENTS.md — Perpustakaan PBO

## Tech Stack
- Java 17+
- Gson 2.10.1 + Gson Extras (RuntimeTypeAdapterFactory)
- CLI (terminal-based, no GUI)
- JSON file persistence

## Build & Run
```bash
# Compile
javac -cp "lib/*" -d out -sourcepath src src/**/*.java

# Run
java -cp "out:lib/*" Main
```

## Project Structure
```
src/
├── Main.java              — Entry point (not yet created)
├── model/                 — Domain classes
│   ├── abstract/
│   │   └── ItemPerpustakaan.java
│   ├── BukuFisik.java
│   ├── BukuDigital.java
│   ├── Jurnal.java
│   ├── Anggota.java
│   ├── Admin.java
│   ├── Peminjaman.java
│   └── StatusPeminjaman.java
├── interfaces/
│   ├── Identifiable.java
│   ├── IBorrowable.java
│   └── ISearchable.java
├── collection/
│   └── Repository.java
├── exception/
│   ├── BukuTidakTersediaException.java
│   ├── AnggotaTidakValidException.java
│   ├── PeminjamanMelebihiBatasException.java
│   └── BukuTidakDitemukanException.java
├── service/
│   ├── PerpustakaanService.java
│   └── AuthService.java
└── ui/
    ├── MenuManager.java   (not yet created)
    ├── MenuAdmin.java     (not yet created)
    └── MenuAnggota.java   (not yet created)

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
- [ ] Dependency (UI → Service) — pending (Phase 4)
- [ ] Message Passing — pending (Phase 4)

## References
- Course materials: `/reference/` (10 PDF files on OOP concepts)
