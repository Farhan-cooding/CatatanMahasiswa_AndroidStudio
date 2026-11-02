package com.example.catatanmahasiswa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Kumpulan warna khusus yang digunakan dalam aplikasi
private val Purple = Color(0xFF5145CD)
private val Green = Color(0xFF2E946B)
private val Orange = Color(0xFFD99A18)
private val Violet = Color(0xFF8060C9)
private val Teal = Color(0xFF00897B) // Warna khusus untuk kategori Kelas

private val Muted = Color(0xFF85899A)

// Struktur data (Model) untuk satu item Catatan
private data class Note(
    val id: Int,
    val title: String,
    val category: String,
    val content: String,
    val date: String
)

// Data dummy/sementara untuk menampilkan list catatan sebelum ada database asli
private val sampleNotes = listOf(
    Note(
        1, "Pemrograman Mobile", "Perkuliahan",
        "Belajar Jetpack Compose, komponen UI, state, " +
                "dan navigasi aplikasi Android.",
        "Hari ini"
    ),
    Note(
        2, "Basis Data", "Tugas",
        "Membuat rangkuman SQL, normalisasi, " +
                "dan latihan query database.",
        "Kemarin"
    ),
    Note(
        3, "Algoritma", "Perkuliahan",
        "Mempelajari struktur data dan kompleksitas algoritma.",
        "Senin"
    ),
    Note(
        4, "Ide Proyek", "Pribadi",
        "Mencatat ide aplikasi sederhana untuk tugas kuliah.",
        "Minggu lalu"
    ),
    Note(
        5, "Jadwal Pengganti", "Kelas",
        "Kuliah pengganti ruang Lab 3 hari Rabu jam 13.00, membawa laptop masing-masing.",
        "Hari ini"
    )
)

// Daftar kategori yang tersedia untuk filter dan form tambah catatan
private val categories = listOf(
    "Semua", "Perkuliahan", "Tugas", "Pribadi", "Kelas"
)

// Fungsi untuk menentukan warna ikon/label berdasarkan nama kategori
// Jika ingin menambah kategori baru dengan warna beda, tambahkan di dalam when ini
private fun noteColor(category: String): Color =
    when (category) {
        "Perkuliahan" -> Green
        "Tugas" -> Orange
        "Pribadi" -> Violet
        "Kelas" -> Teal
        else -> Purple
    }

// Fungsi untuk menentukan gambar ikon berdasarkan nama kategori
// Jika ingin mengganti ikon kategori, ubah bagian ini
private fun noteIcon(category: String): ImageVector =
    when (category) {
        "Perkuliahan" -> Icons.AutoMirrored.Filled.MenuBook
        "Tugas" -> Icons.AutoMirrored.Filled.Assignment
        "Pribadi" -> Icons.Filled.Lightbulb
        "Kelas" -> Icons.Filled.Class
        else -> Icons.Filled.Description
    }

// Titik masuk utama aplikasi Android
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatatanApp() // Memanggil komponen utama aplikasi
        }
    }
}

// Komponen Utama (Root) yang mengatur state keseluruhan (Navigasi, Tema, Data Input)
@Composable
fun CatatanApp() {
    // State untuk mode gelap/terang
    var darkMode by remember { mutableStateOf(false) }

    // Konfigurasi warna status darkMode
    val colors = if (darkMode) {
        darkColorScheme(
            primary = Color(0xFFB8B0FF),
            background = Color(0xFF11131B),
            surface = Color(0xFF20222D),
            onSurface = Color(0xFFF4F4FA)
        )
    } else {
        lightColorScheme(
            primary = Purple,
            background = Color(0xFFF5F6FA),
            surface = Color.White,
            onSurface = Color(0xFF25263A)
        )
    }

    // Menerapkan tema warna ke seluruh aplikasi
    MaterialTheme(colorScheme = colors) {
        // State Navigasi sederhana: "home" (beranda), "form" (tambah), "detail" (baca)
        var screen by remember { mutableStateOf("home") }

        // State untuk menyimpan ID catatan yang sedang di-klik untuk dilihat detailnya
        var selectedId by remember { mutableIntStateOf(1) }

        // State untuk fitur pencarian dan filter
        var search by remember { mutableStateOf("") }
        var filter by remember { mutableStateOf("Semua") }

        // State sementara untuk menampung inputan form penambahan catatan
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Perkuliahan") }
        var content by remember { mutableStateOf("") }

        // State untuk menampilkan notifikasi pop-up bawah (Snackbar)
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // Mendeteksi orientasi layar (true jika layar landscape/mendatar)
        val landscape =
            LocalConfiguration.current.screenWidthDp >
                    LocalConfiguration.current.screenHeightDp

        // Mencari data catatan spesifik berdasarkan ID yang dipilih
        val selectedNote = sampleNotes.find { it.id == selectedId }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header (Top Bar) dinamis yang berubah sesuai halaman/screen yang aktif
                Header(
                    title = when (screen) {
                        "form" -> "Tambah Catatan"
                        "detail" -> "Detail Catatan"
                        else -> "Catatan Mahasiswa"
                    },
                    subtitle = when (screen) {
                        "form" -> "Tulis catatan kuliahmu"
                        "detail" -> "Baca kembali catatanmu"
                        else -> "Ruang kecil untuk ide besarmu"
                    },
                    back = screen != "home", // Munculkan tombol back jika bukan di home
                    onBack = {
                        screen = "home" // Fungsi kembali ke home
                    },
                    darkMode = darkMode,
                    onTheme = { darkMode = !darkMode } // Fungsi toggle tema
                )

                // Logika perpindahan halaman berdasarkan nilai variabel 'screen'
                when (screen) {
                    "home" -> {
                        // Memfilter daftar catatan berdasarkan teks pencarian DAN kategori
                        val filtered = sampleNotes.filter { note ->
                            note.title.contains(search, ignoreCase = true) &&
                                    (filter == "Semua" || note.category == filter)
                        }

                        // Jika HP di-rotate landscape, tampilkan layout 2 kolom (kiri menu, kanan list)
                        if (landscape) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Kolom Kiri Landscape (Menu, Filter, Statistik)
                                Column(
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    WelcomeCard()
                                    Spacer(Modifier.height(12.dp))
                                    SearchBox(search) { search = it }
                                    Spacer(Modifier.height(10.dp))
                                    CategoryFilters(filter) { filter = it }
                                    Spacer(Modifier.height(12.dp))
                                    Statistics()
                                    Spacer(Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            title = ""
                                            content = ""
                                            category = "Perkuliahan"
                                            screen = "form"
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Add, null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Tambah Catatan")
                                    }
                                    Spacer(Modifier.height(14.dp))
                                }

                                // Kolom Kanan Landscape (Daftar Catatan)
                                Column(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .fillMaxHeight()
                                ) {
                                    Text(
                                        "Catatan Terbaru",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(Modifier.height(10.dp))

                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(filtered, key = { it.id }) { note ->
                                            NoteCard(note) {
                                                selectedId = note.id
                                                screen = "detail"
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Layout standar Portrait (1 kolom scrollable ke bawah)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item { WelcomeCard() }
                                item { Statistics() }

                                item {
                                    SearchBox(search) { search = it }
                                }

                                item {
                                    CategoryFilters(filter) { filter = it }
                                }

                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Catatan Terbaru",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            "${filtered.size} catatan", // Menampilkan jumlah total filter
                                            color = Muted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Jika filter pencarian tidak menemukan hasil
                                if (filtered.isEmpty()) {
                                    item { EmptyState() }
                                } else {
                                    // Menampilkan daftar kartu catatan
                                    items(
                                        filtered,
                                        key = { it.id }
                                    ) { note ->
                                        NoteCard(note) {
                                            selectedId = note.id
                                            screen = "detail" // Pindah ke detail saat kartu di-klik
                                        }
                                    }
                                }

                                // Tombol Tambah Catatan di paling bawah daftar
                                item {
                                    Button(
                                        onClick = {
                                            // Reset form sebelum pindah ke halaman form
                                            title = ""
                                            content = ""
                                            category = "Perkuliahan"
                                            screen = "form"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Tambah Catatan Baru")
                                    }
                                }

                                item {
                                    Text(
                                        "Belajar sedikit demi sedikit ✨",
                                        color = Muted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // Halaman Form Tambah Catatan
                    "form" -> {
                        FormScreen(
                            title = title,
                            onTitle = { title = it }, // Menyimpan inputan judul
                            category = category,
                            onCategory = { category = it }, // Menyimpan inputan kategori
                            content = content,
                            onContent = { content = it }, // Menyimpan inputan isi
                            onCancel = { screen = "home" },
                            onSave = {
                                // Validasi: tidak boleh kosong
                                if (title.isBlank() || content.isBlank()) {
                                    scope.launch {
                                        snackbar.showSnackbar("Judul dan isi wajib diisi")
                                    }
                                } else {
                                    // Berhasil disimpan notif
                                    scope.launch {
                                        snackbar.showSnackbar("catatan berhasil")
                                    }
                                    screen = "home" // Kembali ke home setelah save
                                }
                            }
                        )
                    }

                    // Halaman Detail Baca Catatan
                    "detail" -> {
                        if (selectedNote != null) {
                            DetailScreen(
                                note = selectedNote, // Data catatan yang akan ditampilkan
                                landscape = landscape,
                                onBack = { screen = "home" }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fungsi Komponen Header (Bagian atas aplikasi)
// Mengatur tombol back, judul, subjudul, dan tombol ganti tema (terang/gelap)
@Composable
private fun Header(
    title: String,
    subtitle: String,
    back: Boolean,
    onBack: () -> Unit,
    darkMode: Boolean,
    onTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Purple)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tombol Back muncul jika parameter back = true
        if (back) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
        } else {
            // Ikon statis jika sedang di home
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = 0.17f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Teks Judul dan Subjudul
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Tombol toggle tema hanya muncul di halaman Home (back = false)
        if (!back) {
            IconButton(onClick = onTheme) {
                Icon(
                    if (darkMode) Icons.Filled.LightMode
                    else Icons.Filled.DarkMode,
                    contentDescription = "Ganti tema",
                    tint = Color.White
                )
            }
        }
    }
}

// Fungsi Komponen Kartu Ucapan Selamat Datang (Di halaman Home paling atas)
@Composable
private fun WelcomeCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (MaterialTheme.colorScheme.background == Color(0xFFF5F6FA))
                    Color(0xFFE9E8FF)
                else MaterialTheme.colorScheme.surface
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Halo, bunda teddy! 👋",
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Sudah mencatat materi kuliah hari ini?",
                fontSize = 12.sp,
                color = Muted
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.White
            ) {
                Text(
                    "✦ Semangat belajar!",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    fontSize = 11.sp,
                    color = Purple,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Ikon sekolah di sebelah kanan teks ucapan
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(Color(0xFFD6D2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.School,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

// Fungsi Komponen deretan statistik jumlah catatan (Semua, Tugas, Kuliah)
@Composable
private fun Statistics() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        StatCard(
            "Semua", "4",
            Icons.Filled.Description,
            Purple, Modifier.weight(1f)
        )
        StatCard(
            "Tugas", "1",
            Icons.AutoMirrored.Filled.Assignment,
            Orange, Modifier.weight(1f)
        )
        StatCard(
            "Kuliah", "2",
            Icons.AutoMirrored.Filled.MenuBook,
            Green, Modifier.weight(1f)
        )
    }
}

// Fungsi Komponen Kartu Statistik Tunggal (Dipanggil 3x oleh Statistics)
@Composable
private fun StatCard(
    label: String, // Label bawah (misal: "Tugas")
    value: String, // Angka jumlah (misal: "1")
    icon: ImageVector, // Gambar ikon
    tint: Color, // Warna ikon
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(7.dp))
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(label, fontSize = 11.sp, color = Muted)
        }
    }
}

// Fungsi Komponen Input Pencarian (Search Bar)
@Composable
private fun SearchBox(
    value: String, // Nilai text pencarian saat ini
    onChange: (String) -> Unit // Fungsi callback saat teks diketik
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Cari catatan kuliah...") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            // Tampilkan tombol "X" untuk hapus jika kotak tidak kosong
            if (value.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) {
                    Icon(Icons.Filled.Close, "Hapus pencarian")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(15.dp)
    )
}

// Fungsi Komponen Deretan Filter Kategori (Pill Buttons)
@Composable
private fun CategoryFilters(
    selected: String, // Kategori yang sedang aktif dipilih
    onSelect: (String) -> Unit // Fungsi callback ketika kategori di-klik
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Melakukan loop (perulangan) pada daftar kategori
        categories.forEach { item ->
            val active = selected == item // Cek apakah item ini sedang dipilih

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .clickable { onSelect(item) }, // Ubah state filter saat di-klik
                shape = RoundedCornerShape(50.dp),
                color = if (active) Purple else MaterialTheme.colorScheme.surface,
                border = if (active) null else BorderStroke(1.dp, Color(0xFFE1E2EA))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item,
                        fontSize = 10.sp,
                        maxLines = 1,
                        color = if (active) Color.White else Muted,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// Fungsi Komponen Tampilan Satu Item/Kartu Catatan di Daftar List
@Composable
private fun NoteCard(
    note: Note, // Data catatan tunggal
    onClick: () -> Unit // Fungsi callback ketika kartu di-klik (biasanya buka detail)
) {
    val tint = noteColor(note.category) // Ambil warna sesuai kategorinya

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon kategori di sebelah kiri
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    noteIcon(note.category),
                    contentDescription = note.category,
                    tint = tint,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Judul dan isi catatan di tengah
            Column(Modifier.weight(1f)) {
                Text(
                    note.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // Potong teks pakai '...' jika panjang
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    note.content,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(7.dp))

                // Label kategori dan tanggal di bawah konten
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = tint.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            note.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = tint
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "• ${note.date}",
                        color = Muted,
                        fontSize = 10.sp
                    )
                }
            }

            // Ikon panah (chevron) di sebelah kanan
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Lihat detail",
                tint = Muted
            )
        }
    }
}

// Fungsi Komponen Tampilan Kosong (Jika hasil pencarian/filter tidak ada)
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = Muted
        )
        Spacer(Modifier.height(8.dp))
        Text("Catatan tidak ditemukan", fontWeight = FontWeight.Bold)
        Text(
            "Coba kata kunci atau kategori lain.",
            color = Muted,
            fontSize = 12.sp
        )
    }
}

// Fungsi Komponen Halaman Form Tambah Catatan Baru
@Composable
private fun FormScreen(
    title: String, // Value teks judul
    onTitle: (String) -> Unit, // Callback ketika judul diketik
    category: String, // Value dropdown kategori
    onCategory: (String) -> Unit, // Callback ketika kategori dipilih
    content: String, // Value teks isi catatan
    onContent: (String) -> Unit, // Callback ketika isi catatan diketik
    onCancel: () -> Unit, // Callback tombol Batal
    onSave: () -> Unit // Callback tombol Simpan
) {
    // State khusus form untuk menangani buka/tutup menu Dropdown kategori
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        // Field Input Judul
        item {
            Text(
                "Judul Mata Kuliah",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitle,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: Pemrograman Mobile") },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Purple)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        }

        // Field Input Kategori (Dropdown Menu)
        item {
            Text(
                "Kategori",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))

            Box {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }, // Klik untuk buka menu
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(noteIcon(category), null, tint = noteColor(category))
                        Spacer(Modifier.width(10.dp))
                        Text(category, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ExpandMore, null)
                    }
                }

                // Isi dari Dropdown Menu kategori (Mengecualikan pilihan "Semua")
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.filter { it != "Semua" }.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            leadingIcon = {
                                Icon(noteIcon(item), null, tint = noteColor(item))
                            },
                            onClick = {
                                onCategory(item) // Set kategori terpilih
                                expanded = false // Tutup menu setelah dipilih
                            }
                        )
                    }
                }
            }
        }

        // Field Input Isi Catatan (Multiline Text)
        item {
            Text(
                "Isi Catatan",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = content,
                onValueChange = onContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp), // Minimal tinggi area teks
                placeholder = { Text("Tulis ringkasan materi atau tugas...") },
                leadingIcon = { Icon(Icons.Filled.EditNote, null, tint = Purple) },
                minLines = 5,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        }

        // Kumpulan Tombol Batal dan Simpan
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Close, null)
                    Spacer(Modifier.width(5.dp))
                    Text("Batal")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Save, null)
                    Spacer(Modifier.width(5.dp))
                    Text("Simpan")
                }
            }
        }

        item {
            Text(
                "💡 Tips: gunakan judul singkat agar mudah ditemukan.",
                fontSize = 12.sp,
                color = Muted
            )
        }
    }
}

// Fungsi Komponen Halaman Tampilan Detail Catatan Lengkap
@Composable
private fun DetailScreen(
    note: Note, // Data catatan yang akan di-render di detail
    landscape: Boolean, // Cek status orientasi layar
    onBack: () -> Unit // Callback kembali ke Home
) {
    val tint = noteColor(note.category)

    // Logika responsif: Jika diputar Landscape tampilkan 2 kolom
    if (landscape) {
        Row(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Kiri: Judul dan Identitas (DetailHeading)
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                DetailHeading(note, tint)
            }

            // Kanan: Teks isi catatan yang panjang & Tombol kembali
            Column(
                modifier = Modifier.weight(1.1f).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Isi Catatan", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(note.content, lineHeight = 24.sp)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Home, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Kembali ke Beranda")
                }
            }
        }
    } else {
        // Mode Standar Portrait (Tampilan Scroll ke bawah biasa)
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    DetailHeading(note, tint)
                    HorizontalDivider(Modifier.padding(vertical = 18.dp))
                    Text("Isi Catatan", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(9.dp))
                    Text(
                        note.content,
                        fontSize = 14.sp,
                        lineHeight = 23.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Home, null)
                Spacer(Modifier.width(8.dp))
                Text("Kembali ke Beranda")
            }
        }
    }
}

// Fungsi Sub-komponen (Child) dari DetailScreen untuk menampilkan Judul besar, ikon, dan tanggal
@Composable
private fun DetailHeading(note: Note, tint: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(tint.copy(alpha = 0.10f))
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                noteIcon(note.category),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            note.title,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "◷  ${note.date}",
            color = Muted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(12.dp))

        Surface(
            color = tint,
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                note.category,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                fontSize = 12.sp
            )
        }
    }
}


// KUMPULAN FUNGSI PREVIEW
// Preview untuk tampilan hp (potrID)
@Preview(showBackground = true, name = "Mode Portrait")
@Composable
fun CatatanAppPortraitPreview() {
    CatatanApp()
}

// Preview untuk tampilan hp (Landscape)
@Preview(
    showBackground = true,
    name = "Mode Landscape",
    device = "spec:width=800dp,height=400dp"
)
@Composable
fun CatatanAppLandscapePreview() {
    CatatanApp()
}
