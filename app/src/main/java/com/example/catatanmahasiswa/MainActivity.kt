package com.example.catatanmahasiswa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Mengatur Tema Warna Aplikasi sesuai palet Material 3 pada ketentuan tugas
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6750A4),
                    primaryContainer = Color(0xFFEADDFF),
                    surface = Color(0xFF1C1B1F),
                    background = Color(0xFFF4F3F7)
                )
            ) {
                // Memanggil fungsi utama Navigasi antar layar
                AppNavigation()
            }
        }
    }
}

// Data Class: Model data sederhana untuk menampung informasi catatan
data class Catatan(val id: Int, val judul: String, val kategori: String, val isi: String, val tanggal: String)

@Composable
fun AppNavigation() {
    // NavController: Komponen Navigation Compose untuk mengatur perpindahan antar layar (Screen)
    val navController = rememberNavController()

    // State List: Menyimpan daftar catatan sementara di memori (Tanpa database CRUD yang rumit sesuai arahan dosen)
    val daftarCatatan = remember {
        mutableStateListOf(
            Catatan(1, "Pemrograman Mobile", "Perkuliahan", "Belajar dasar Jetpack Compose, Column, Row, dan LazyColumn.", "10 Des 2026"),
            Catatan(2, "Basis Data", "Tugas", "Mengerjakan Normalisasi Database bentuk 1NF, 2NF, dan 3NF.", "11 Des 2026"),
            Catatan(3, "Algoritma", "Kuis", "Persiapan kuis sorting dan searching.", "13 Des 2026")
        )
    }

    // NavHost: Wadah untuk mendaftarkan rute (route) dari setiap layar aplikasi
    NavHost(navController = navController, startDestination = "beranda") {

        // Rute 1: Layar Beranda
        composable("beranda") {
            LayarBeranda(navController = navController, listCatatan = daftarCatatan)
        }

        // Rute 2: Layar Tambah Catatan
        composable("tambah") {
            LayarTambahCatatan(navController = navController, onSimpan = { judul, kategori, isi ->
                val baru = Catatan(
                    id = daftarCatatan.size + 1,
                    judul = judul,
                    kategori = kategori,
                    isi = isi,
                    tanggal = "16 Okt 2026"
                )
                daftarCatatan.add(baru) // Menambahkan data baru ke dalam list sementara
                navController.popBackStack() // Kembali ke layar beranda setelah disimpan
            })
        }

        // Rute 3: Layar Detail Catatan (Menerima parameter data yang diklik)
        composable("detail/{judul}/{kategori}/{isi}/{tanggal}") { backStackEntry ->
            val judul = backStackEntry.arguments?.getString("judul") ?: ""
            val kategori = backStackEntry.arguments?.getString("kategori") ?: ""
            val isi = backStackEntry.arguments?.getString("isi") ?: ""
            val tanggal = backStackEntry.arguments?.getString("tanggal") ?: ""

            LayarDetailCatatan(navController = navController, judul = judul, kategori = kategori, isi = isi, tanggal = tanggal)
        }
    }
}

// ==========================================
// 1. LAYAR BERANDA (HOME SCREEN)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarBeranda(navController: NavController, listCatatan: List<Catatan>) {
    // Scaffold: Kerangka dasar tata letak UI (menyediakan struktur untuk TopBar, FAB, dll)
    Scaffold(
        topBar = {
            // TopAppBar: Komponen bar bagian atas aplikasi
            TopAppBar(
                title = { Text("Catatan Mahasiswa") }, // Judul Aplikasi
                actions = {
                    // IconButton & Icon: Tombol profil di pojok kanan atas
                    IconButton(onClick = { /* Aksi klik profil */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profil")
                    }
                }
            )
        },
        floatingActionButton = {
            // FloatingActionButton (FAB): Tombol melayang berbentuk bulat untuk menambah catatan baru
            FloatingActionButton(onClick = {
                // Fungsi: Berpindah dari Layar Beranda ke Layar Tambah Catatan
                navController.navigate("tambah")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
            }
        }
    ) { padding ->
        // Column: Menyusun komponen secara vertikal dari atas ke bawah
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Card: Kotak/Kartu untuk menampilkan sapaan pengguna
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Row: Menyusun elemen secara horizontal (berjajar ke samping) di dalam Card
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Halo, Farhan!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Yuk catat aktivitas perkuliahanmu hari ini.", fontSize = 14.sp)
                    }
                    Icon(Icons.Default.Face, contentDescription = "Avatar", modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Catatan Terbaru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // LazyColumn: Daftar list yang efisien untuk menampilkan data catatan secara dinamis dan bisa di-scroll
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listCatatan) { item ->
                    // Card untuk setiap item catatan yang bisa diklik
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // Fungsi: Membuka layar Detail Catatan dengan membawa data item yang dipilih
                            navController.navigate("detail/${item.judul}/${item.kategori}/${item.isi}/${item.tanggal}")
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.judul, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Kategori: ${item.kategori}", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.tanggal, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. LAYAR TAMBAH CATATAN (FORM SCREEN)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarTambahCatatan(navController: NavController, onSimpan: (String, String, String) -> Unit) {
    // State untuk menampung input teks dari pengguna
    var judul by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Perkuliahan") }
    var isi by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Catatan") },
                navigationIcon = {
                    // IconButton (Tombol Panah Kembali): Berfungsi untuk kembali ke halaman sebelumnya
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // OutlinedTextField 1: Kotak input untuk Judul Mata Kuliah
            OutlinedTextField(
                value = judul,
                onValueChange = { judul = it },
                label = { Text("Judul Mata Kuliah") },
                modifier = Modifier.fillMaxWidth()
            )

            // OutlinedTextField 2: Kotak input untuk Kategori Catatan
            OutlinedTextField(
                value = kategori,
                onValueChange = { kategori = it },
                label = { Text("Kategori (cth: Perkuliahan/Tugas)") },
                modifier = Modifier.fillMaxWidth()
            )

            // OutlinedTextField 3: Kotak input untuk Isi Catatan yang lebih besar
            OutlinedTextField(
                value = isi,
                onValueChange = { isi = it },
                label = { Text("Isi Catatan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            // Row untuk menyejajarkan dua tombol aksi di bagian bawah form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // OutlinedButton (Tombol Batal): Berfungsi membatalkan input dan kembali ke beranda
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal")
                }

                // Button (Filled Button / Tombol Simpan): Berfungsi menyimpan data inputan baru ke dalam list
                Button(
                    onClick = {
                        if (judul.isNotBlank()) {
                            onSimpan(judul, kategori, isi)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simpan Catatan")
                }
            }
        }
    }
}

// ==========================================
// 3. LAYAR DETAIL CATATAN (DETAIL SCREEN)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarDetailCatatan(navController: NavController, judul: String, kategori: String, isi: String, tanggal: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Catatan") },
                navigationIcon = {
                    // Tombol Kembali ke halaman sebelumnya
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Menampilkan informasi detail dari catatan yang dipilih
            Text(judul, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(tanggal, fontSize = 12.sp, color = Color.Gray)

            Divider() // Garis pembatas visual

            Text("Kategori: $kategori", fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Isi Catatan:", fontWeight = FontWeight.Bold)
            Text(isi)

            Spacer(modifier = Modifier.weight(1f))

            // Baris tombol aksi di halaman detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // OutlinedButton (Tombol Edit): Berfungsi untuk opsi edit catatan (Bersifat UI/opsional sesuai tugas)
                OutlinedButton(
                    onClick = { /* Aksi edit catatan */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                // Button (Tombol Hapus Berwarna Merah): Berfungsi menghapus/keluar dari halaman detail kembali ke beranda
                Button(
                    onClick = {
                        navController.popBackStack("beranda", inclusive = false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)), // Warna merah khas tombol hapus
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus")
                }
            }
        }
    }
}