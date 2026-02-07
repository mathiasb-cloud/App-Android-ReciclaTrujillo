package com.cibertec.ecotrujilloapp.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.cibertec.ecotrujilloapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var edtNombre: TextInputEditText
    private lateinit var edtApellido: TextInputEditText
    private lateinit var edtTelefono: TextInputEditText
    private lateinit var edtCorreo: TextInputEditText
    private lateinit var btnEditar: Button
    private lateinit var btnGuardar: Button

    private var modoEdicion = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        edtNombre = view.findViewById(R.id.edtNombre)
        edtApellido = view.findViewById(R.id.edtApellido)
        edtTelefono = view.findViewById(R.id.edtTelefono)
        edtCorreo = view.findViewById(R.id.edtCorreo)
        btnEditar = view.findViewById(R.id.btnEditar)
        btnGuardar = view.findViewById(R.id.btnGuardar)

        val usuario = auth.currentUser

        if (usuario != null) {
            edtCorreo.setText(usuario.email)
            cargarDatosUsuario(usuario.uid)
        }

        btnEditar.setOnClickListener {
            habilitarCampos(true)
            btnGuardar.isEnabled = true
            modoEdicion = true
        }

        btnGuardar.setOnClickListener {
            if (usuario != null) {
                guardarCambios(usuario.uid)
            }
        }

        return view
    }

    private fun cargarDatosUsuario(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                edtNombre.setText(doc.getString("nombre") ?: "")
                edtApellido.setText(doc.getString("apellido") ?: "")
                edtTelefono.setText(doc.getString("telefono") ?: "")
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarCambios(uid: String) {
        val datos = hashMapOf(
            "nombre" to edtNombre.text.toString().trim(),
            "apellido" to edtApellido.text.toString().trim(),
            "telefono" to edtTelefono.text.toString().trim(),
        )

        db.collection("usuarios").document(uid)
            .update(datos as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                habilitarCampos(false)
                btnGuardar.isEnabled = false
                modoEdicion = false
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar los cambios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun habilitarCampos(estado: Boolean) {
        edtNombre.isEnabled = estado
        edtApellido.isEnabled = estado
        edtTelefono.isEnabled = estado
    }
}
