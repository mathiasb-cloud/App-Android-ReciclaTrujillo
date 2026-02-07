package com.cibertec.ecotrujilloapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val nombres = findViewById<EditText>(R.id.etNombres)
        val apellidos = findViewById<EditText>(R.id.etApellidos)
        val telefono = findViewById<EditText>(R.id.etTelefono)
        val usuario = findViewById<EditText>(R.id.etUsuarioRegistro)
        val password = findViewById<EditText>(R.id.etPasswordRegistro)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguiente)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnSiguiente.setOnClickListener {
            val email = usuario.text.toString().trim()
            val pass = password.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty() ||
                nombres.text.isEmpty() || apellidos.text.isEmpty() || telefono.text.isEmpty()
            ) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    // Guardar datos extra en Firestore
                    val datosUsuario = hashMapOf(
                        "uid" to uid,
                        "nombres" to nombres.text.toString(),
                        "apellidos" to apellidos.text.toString(),
                        "telefono" to telefono.text.toString(),
                        "email" to email
                    )

                    db.collection("usuarios").document(uid)
                        .set(datosUsuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registro exitoso 🎉", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar datos", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al registrar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
