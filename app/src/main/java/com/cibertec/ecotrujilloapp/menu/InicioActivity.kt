package com.cibertec.ecotrujilloapp.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.ecotrujilloapp.LoginActivity
import com.cibertec.ecotrujilloapp.R
import com.google.firebase.auth.FirebaseAuth

class InicioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        auth = FirebaseAuth.getInstance()

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val btnInvitado = findViewById<Button>(R.id.btnContinuarInvitado)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnInvitado.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu nombre", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("nombreUsuario", nombre)
                intent.putExtra("esAdmin", false)
                startActivity(intent)
                finish()
            }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
