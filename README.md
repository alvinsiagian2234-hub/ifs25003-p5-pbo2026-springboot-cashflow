# ifs25003-p5-pbo2026-springboot-cashflow

Aplikasi web **Cashflow (Catatan Keuangan)** berbasis **Spring Boot 4.1.2**, **Maven**, **Vaadin 25.3.4**, dan **JDBC (PostgreSQL)**, dengan arsitektur **Clean Architecture** (domain, usecase, adapter, framework).

| | |
| --- | --- |
| Folder | `ifs25003-p5-pbo2026-springboot-cashflow` |
| Artifact Maven | `ifs25003-p5-pbo2026-springboot-cashflow` |
| Package | `org.delcom.ifs25003springbootcashflow` |
| Database | `pbo2026_cashflow` |

## Prasyarat

- JDK 25
- PostgreSQL — buat database **sebelum** menjalankan aplikasi:

```sql
CREATE DATABASE pbo2026_cashflow;
```

Contoh lewat PowerShell:

```powershell
$env:PGPASSWORD="password_anda"; & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE pbo2026_cashflow;"
```

## Konfigurasi `.env`

Salin `.env.example` menjadi `.env` (file `.env` diabaikan git), lalu sesuaikan `DB_PASSWORD`:

```env
APP_PORT=8080

DB_CONNECTION=pgsql
DB_HOST=localhost
DB_PORT=5432
DB_DATABASE=pbo2026_cashflow
DB_USERNAME=postgres
DB_PASSWORD=postgres

ROOT_USERNAME=admin
ROOT_PASSWORD=admin

UPLOAD_DIR=uploads
```

- Akun admin di-seed otomatis saat start dari `ROOT_USERNAME` / `ROOT_PASSWORD`.
- Upload (foto profil & bukti transaksi) disimpan di `UPLOAD_DIR` dan dilayani di `/uploads/**`.
- `DotEnv` hanya mengisi system property jika environment variable / property dengan nama yang sama **belum** ada.

## Fitur

| Fitur | Keterangan |
| --- | --- |
| Auth | Register, login, logout (`AuthSession`: VaadinSession + HTTP session + cookie `cashflow_uid` + flag `LOGGED_OUT_KEY`); sesi bertahan setelah refresh |
| Tambah Pemasukan / Pengeluaran | Dialog keterangan + jumlah + upload bukti opsional (`proofPath`) |
| Bukti zoom | Klik thumbnail bukti → `ImagePreviewDialog` (klik gambar toggle 1.0x ↔ 1.8x, tombol Perkecil / Perbesar / Tutup) |
| Cari | Filter keterangan (case-insensitive), hanya data milik user login |
| Urutkan | Klik header kolom grid dan/atau pilih `SortOption` (7 opsi: jumlah naik/turun, pemasukan dulu, pengeluaran dulu, terbaru, terlama, keterangan A-Z) |
| Saldo | Kartu ringkasan: jumlah transaksi, total pemasukan, total pengeluaran, saldo (format `Rp 1,000,000.00`) |
| Ubah / Hapus | Ubah lewat dialog (atau double-click baris); hapus dengan dialog konfirmasi; hanya data milik sendiri |
| Profil | Ubah nama, password, foto profil |
| ADMIN | Menu Daftar Pengguna → lihat transaksi user lain (read-only); admin juga punya cashflow sendiri |
| Validasi | Keterangan kosong, jumlah kosong / non-numerik / non-positif / terlalu besar, ID tidak valid → notifikasi error |

### Hak akses rute

| Kategori | Path | View | Akses |
| --- | --- | --- | --- |
| Public | `/login`, `/register` | `LoginView`, `RegisterView` | Anonim (jika sudah login → `/`) |
| Public | `/uploads/**`, `/icons/**`, `styles.css` | Static | Semua |
| Protected | `/` | `FinanceView` | USER & ADMIN (belum login → `/login`) |
| Protected | `/profile` | `ProfileView` | USER & ADMIN |
| Admin | `/users` | `UsersView` | ADMIN saja (USER → `/`) |
| Admin | `/user-transactions/{id}` | `UserTransactionsView` | ADMIN saja, read-only (USER → `/`) |

## Struktur

```text
src/main/java/org/delcom/ifs25003springbootcashflow/
├── domain/
│   ├── PasswordHasher.java
│   ├── entity/         # Transaction, TransactionType, BalanceSummary, User, Role, SortOption
│   ├── exception/      # DomainException, EntityNotFoundException, ValidationException
│   └── repository/     # IRepository<T, ID>, ITransactionRepository, IUserRepository
├── usecase/            # AuthUseCase, FinanceUseCase, UserUseCase
├── adapter/
│   ├── presenter/      # FinancePresenter (format Rp 1,000,000.00, pesan)
│   ├── repository/     # InMemory*, TransactionRepository, UserRepository (unit test)
│   │                   # Jdbc*Repository (runtime), SchemaInitializer
│   └── storage/        # FileStorage
├── framework/
│   ├── auth/           # AuthSession
│   └── view/           # LoginView, RegisterView, MainLayout, FinanceView, ProfileView,
│                       # UsersView, UserTransactionsView, PageCard, GridStyles, AppDialog,
│                       # SummaryCards, TransactionColumns, ImagePreviewDialog,
│                       # LoadingIndicatorInitListener
├── config/AppConfig.java   # composition root (@Bean)
├── DotEnv.java
└── App.java                # @StyleSheet(Lumo) + styles.css, @PWA, DotEnv.load()
```

Dependency Rule: `domain ← usecase ← adapter / framework`. Domain tidak mengimpor Vaadin/Spring.

## Menjalankan

```powershell
cd ifs25003-p5-pbo2026-springboot-cashflow
.\mvnw.cmd spring-boot:run
```

Buka [http://localhost:8080](http://localhost:8080) (atau port `APP_PORT`). Login admin: `ROOT_USERNAME` / `ROOT_PASSWORD`.

### Build JAR (profil production Vaadin)

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs25003-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

Tanpa `-Pproduction`, `java -jar` biasanya gagal (`vaadin-dev-server not found`).

## Uji & coverage

```powershell
.\mvnw.cmd clean verify
```

- 68 test: unit test (repository InMemory), tes JDBC (H2, `src/test/resources/application-test.yaml`), `AppTest` (`@SpringBootTest` + profil `test`)
- Jacoco mengecualikan `framework/view` dan `framework/auth`; counter terukur (INSTRUCTION, BRANCH, LINE, METHOD, CLASS) = **100%**
- Laporan: `target/site/jacoco/index.html`
