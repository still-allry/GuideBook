# Book Guide

Plugin dokumentasi server Minecraft dalam satu buku. Satu command membuka seluruh panduan, dengan daftar isi dan navigasi halaman yang bisa diklik.

- Name: Book Guide
- Author: allry_
- Command: `/guide`
- Permission pemain: `bookguide.use`
- Permission admin: `bookguide.admin`

## Kebutuhan

- Paper 1.21.4 atau versi yang kompatibel
- Java 21
- Maven 3.9 atau lebih baru untuk build

## Instalasi

1. Salin `BookGuide-1.0.0.jar` ke folder `plugins` server.
2. Jalankan server sekali agar `plugins/BookGuide/guide.yml` dibuat.
3. Edit `guide.yml`.
4. Jalankan `/guide reload` sebagai admin.

## Command

| Command | Fungsi | Permission |
|---|---|---|
| `/guide` | Membuka buku Server Guide | `bookguide.use` |
| `/guide reload` | Memuat ulang `guide.yml` dan memvalidasi isi | `bookguide.admin` |

## Pengelolaan konten

Semua dokumentasi berada di satu file `guide.yml`. Setiap kategori punya daftar `pages`, dan setiap halaman punya daftar `lines`.

- Ubah urutan kategori dengan mengubah urutan item di `sections`.
- Tambah halaman dengan menambah item baru di `pages`.
- Hapus halaman dengan menghapus item dari `pages`.
- Ubah command atau deskripsi dengan mengedit baris teks.
- Teks mendukung MiniMessage dan kode warna lama seperti `&a` atau `&l`.
- Daftar isi menampilkan satu baris per bab, berisi judul dan nomor halaman yang bisa diklik. Untuk 7 bab default, daftar isi muat dalam satu halaman.
- Nomor halaman, header bab, tombol kembali ke daftar isi, dan tombol halaman sebelumnya atau berikutnya dibuat otomatis.
- Plugin menolak konten yang terlalu panjang atau melebihi 100 halaman. Perbaiki `guide.yml` lalu jalankan `/guide reload` lagi.

Contoh format:

```yaml
sections:
  - title: "Contoh Bab"
    pages:
      - lines:
          - "<yellow><bold>Judul Halaman</bold></yellow>"
          - "<gray>Penjelasan singkat.</gray>"
```

Satu item di `pages` menjadi satu halaman buku. Jika isi terlalu panjang, plugin membaginya menjadi beberapa halaman buku secara otomatis dan seimbang.

### Menulis parameter command

Kurung sudut adalah tag MiniMessage. Tulis parameter command dengan backslash agar tampil apa adanya:

```yaml
- '<aqua>/tpa \<player></aqua>'
- '<aqua>/buyclaimblocks \<amount></aqua>'
```

Tanpa backslash, `<player>` dan `<amount>` dianggap tag dan bisa membuat validasi gagal atau teksnya hilang.
