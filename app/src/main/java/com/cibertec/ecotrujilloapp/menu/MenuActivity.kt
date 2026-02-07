package com.cibertec.ecotrujilloapp.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.cibertec.ecotrujilloapp.LoginActivity
import com.cibertec.ecotrujilloapp.R
import com.cibertec.ecotrujilloapp.fragments.MapaFragment
import com.cibertec.ecotrujilloapp.fragments.ModulosFragment
import com.cibertec.ecotrujilloapp.fragments.PerfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView
    private var esAdmin = false

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        // Inicializa Google Maps Renderer
        com.google.android.gms.maps.MapsInitializer.initialize(
            this,
            com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
        ) { }

        // Referencias
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        btnMenu = findViewById(R.id.btnMenu)

        // Propiedades visuales
        btnMenu.bringToFront()
        bottomNavigation.bringToFront()
        btnMenu.isClickable = true
        btnMenu.isFocusable = true
        btnMenu.elevation = 20f
        bottomNavigation.elevation = 16f

        // Botón de menú lateral
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        // Recibir si es admin
        esAdmin = intent.getBooleanExtra("esAdmin", false)

        // Header del Drawer
        val header = navigationView.getHeaderView(0)
        val txtNombreHeader = header.findViewById<android.widget.TextView>(R.id.txtNombreUsuario)
        val txtCorreoHeader = header.findViewById<android.widget.TextView>(R.id.txtCorreoUsuario)

        if (esAdmin) {
            txtNombreHeader.text = "Administrador"
            txtCorreoHeader.text = "admin@ecotrujillo.com"
        } else {
            val usuario = auth.currentUser
            txtCorreoHeader.text = usuario?.email ?: "Correo no disponible"
            usuario?.let {
                db.collection("usuarios").document(it.uid).get()
                    .addOnSuccessListener { doc ->
                        txtNombreHeader.text = doc.getString("nombre") ?: "Usuario"
                    }
            }
        }

        // Fragment inicial
        if (savedInstanceState == null) {
            val fragmentInicial = ModulosFragment().apply {
                arguments = Bundle().apply { putBoolean("esAdmin", esAdmin) }
            }
            replaceFragment(fragmentInicial)
            bottomNavigation.selectedItemId = R.id.nav_modulos
        }

        // Menú inferior
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val f = MapaFragment().apply {
                        arguments = Bundle().apply { putBoolean("esAdmin", esAdmin) }
                    }
                    replaceFragment(f)
                    true
                }
                R.id.nav_modulos -> {
                    val f = ModulosFragment().apply {
                        arguments = Bundle().apply { putBoolean("esAdmin", esAdmin) }
                    }
                    replaceFragment(f)
                    true
                }
                R.id.nav_perfil -> {
                    val f = PerfilFragment().apply {
                        arguments = Bundle().apply { putBoolean("esAdmin", esAdmin) }
                    }
                    replaceFragment(f)
                    true
                }
                else -> false
            }
        }

        // Menú lateral
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    val f = MapaFragment().apply {
                        arguments = Bundle().apply { putBoolean("esAdmin", esAdmin) }
                    }
                    replaceFragment(f)
                    bottomNavigation.selectedItemId = R.id.nav_inicio
                }

                R.id.nav_modulos -> {
                    val f = ModulosFragment().apply {
                        arguments = Bundle().apply { putBoolean("esAdmin", esAdmin) }
                    }
                    replaceFragment(f)
                    bottomNavigation.selectedItemId = R.id.nav_modulos
                }

                R.id.nav_perfil -> {
                    val f = PerfilFragment().apply {
                        arguments = Bundle().apply { putBoolean("esAdmin", esAdmin) }
                    }
                    replaceFragment(f)
                    bottomNavigation.selectedItemId = R.id.nav_perfil
                }

                R.id.nav_logout -> {
                    cerrarSesion()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Si viene desde otra actividad (abrir mapa directo)
        if (intent.getBooleanExtra("abrirMapa", false)) {
            val lat = intent.getDoubleExtra("latitud", 0.0)
            val lon = intent.getDoubleExtra("longitud", 0.0)
            val mapa = MapaFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("esAdmin", esAdmin)
                    putDouble("latitud", lat)
                    putDouble("longitud", lon)
                }
            }
            replaceFragment(mapa)
            bottomNavigation.selectedItemId = R.id.nav_inicio
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.btnMenu)?.bringToFront()
        findViewById<View>(R.id.bottomNavigation)?.bringToFront()
    }

    private fun replaceFragment(fragment: Fragment) {
        // Si el fragmento no tiene el argumento esAdmin, lo agregamos automáticamente
        if (fragment.arguments == null) {
            fragment.arguments = Bundle()
        }
        fragment.arguments?.putBoolean("esAdmin", esAdmin)

        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragments, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    private fun cerrarSesion() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) { }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
