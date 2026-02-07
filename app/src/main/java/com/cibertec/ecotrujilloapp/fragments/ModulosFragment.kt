package com.cibertec.ecotrujilloapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ecotrujilloapp.R
import com.cibertec.ecotrujilloapp.menu.MenuActivity
import com.cibertec.ecotrujilloapp.modulo.Modulo
import com.cibertec.ecotrujilloapp.modulo.ModuloAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ModulosFragment : Fragment() {

    private lateinit var recyclerModulos: RecyclerView
    private lateinit var fabAgregarModulo: FloatingActionButton
    private lateinit var adapter: ModuloAdapter
    private val db = FirebaseFirestore.getInstance()
    private val modulos = mutableListOf<Modulo>()
    private var esAdmin = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_modulos, container, false)

        recyclerModulos = view.findViewById(R.id.recyclerModulos)
        fabAgregarModulo = view.findViewById(R.id.fabAgregarModulo)
        recyclerModulos.layoutManager = GridLayoutManager(requireContext(), 2)


        esAdmin = arguments?.getBoolean("esAdmin", false) ?: false
        Log.d("FAB", "¿Es admin?: $esAdmin")



        fabAgregarModulo.bringToFront()


        fabAgregarModulo.visibility = if (esAdmin) View.VISIBLE else View.GONE



        if (esAdmin) {
            fabAgregarModulo.setOnClickListener { mostrarDialogoAgregar() }
        }



        adapter = ModuloAdapter(modulos) { moduloSeleccionado ->
            if (esAdmin) {
                mostrarDialogoCrud(moduloSeleccionado)
            } else {
                abrirMapa(moduloSeleccionado.getLatitudFinal(), moduloSeleccionado.getLongitudFinal())
            }
        }

        recyclerModulos.adapter = adapter
        cargarModulosDesdeFirebase()


        fabAgregarModulo.bringToFront()
        fabAgregarModulo.elevation = 100f
        fabAgregarModulo.translationZ = 100f


        return view
    }


    private fun cargarModulosDesdeFirebase() {
        db.collection("modulos").get()
            .addOnSuccessListener { result ->
                modulos.clear()
                for (document in result) {
                    val modulo = document.toObject(Modulo::class.java)
                    modulo.id = document.id
                    modulos.add(modulo)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar módulos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoCrud(modulo: Modulo) {
        val opciones = arrayOf("Editar módulo", "Eliminar módulo", "Ver en mapa")

        AlertDialog.Builder(requireContext())
            .setTitle("Opciones para ${modulo.nombre}")
            .setItems(opciones) { _, seleccion ->
                when (seleccion) {
                    0 -> mostrarDialogoEditar(modulo)
                    1 -> eliminarModulo(modulo)
                    2 -> abrirMapa(modulo.getLatitudFinal(), modulo.getLongitudFinal())
                }
            }
            .show()
    }

    private fun mostrarDialogoEditar(modulo: Modulo) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_editar_modulo, null)

        val etNombre = view.findViewById<EditText>(R.id.etNombreModulo)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcionModulo)
        val etLatitud = view.findViewById<EditText>(R.id.etLatitud)
        val etLongitud = view.findViewById<EditText>(R.id.etLongitud)
        val etImagenUrl = view.findViewById<EditText>(R.id.etImagenUrl)

        etNombre.setText(modulo.nombre)
        etDescripcion.setText(modulo.descripcion)
        etLatitud.setText(modulo.latitud.toString())
        etLongitud.setText(modulo.longitud.toString())
        etImagenUrl.setText(modulo.imagenUrl)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar módulo")
            .setView(view)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val botonGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonGuardar.setTextColor(resources.getColor(R.color.green_light))
            botonGuardar.setOnClickListener {
                val nuevosDatos = mapOf(
                    "nombre" to etNombre.text.toString(),
                    "descripcion" to etDescripcion.text.toString(),
                    "latitud" to etLatitud.text.toString().toDoubleOrNull(),
                    "longitud" to etLongitud.text.toString().toDoubleOrNull(),
                    "imagenUrl" to etImagenUrl.text.toString()
                )

                db.collection("modulos").document(modulo.id)
                    .update(nuevosDatos)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Módulo actualizado", Toast.LENGTH_SHORT).show()
                        cargarModulosDesdeFirebase()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al actualizar módulo", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoAgregar() {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_editar_modulo, null)

        val etNombre = view.findViewById<EditText>(R.id.etNombreModulo)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcionModulo)
        val etLatitud = view.findViewById<EditText>(R.id.etLatitud)
        val etLongitud = view.findViewById<EditText>(R.id.etLongitud)
        val etImagenUrl = view.findViewById<EditText>(R.id.etImagenUrl)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Agregar módulo")
            .setView(view)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val botonGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonGuardar.setTextColor(resources.getColor(R.color.green_light))
            botonGuardar.setOnClickListener {
                val nuevoModulo = Modulo(
                    nombre = etNombre.text.toString(),
                    descripcion = etDescripcion.text.toString(),
                    latitud = etLatitud.text.toString().toDoubleOrNull() ?: 0.0,
                    longitud = etLongitud.text.toString().toDoubleOrNull() ?: 0.0,
                    imagenUrl = etImagenUrl.text.toString()
                )

                db.collection("modulos").add(nuevoModulo)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Módulo agregado", Toast.LENGTH_SHORT).show()
                        cargarModulosDesdeFirebase()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al agregar módulo", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.show()
    }


    private fun eliminarModulo(modulo: Modulo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar módulo")
            .setMessage("¿Deseas eliminar este módulo?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("modulos").document(modulo.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Módulo eliminado", Toast.LENGTH_SHORT).show()
                        cargarModulosDesdeFirebase()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al eliminar módulo", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun abrirMapa(latitud: Double, longitud: Double) {
        if (latitud == 0.0 || longitud == 0.0) {
            Toast.makeText(requireContext(), "⚠ Coordenadas vacías o no válidas", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(requireContext(), MenuActivity::class.java)
        intent.putExtra("abrirMapa", true)
        intent.putExtra("latitud", latitud)
        intent.putExtra("longitud", longitud)
        startActivity(intent)
    }
}
