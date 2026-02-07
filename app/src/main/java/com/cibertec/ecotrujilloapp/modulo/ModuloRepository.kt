package com.cibertec.ecotrujilloapp.repository

import com.cibertec.ecotrujilloapp.modulo.Modulo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ModuloRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("modulos")


    suspend fun obtenerTodos(): List<Modulo> {
        return try {
            val snapshot = coleccion.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Modulo::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔹 Agregar un nuevo módulo
    fun agregar(modulo: Modulo, onComplete: (Boolean) -> Unit) {
        coleccion.add(modulo)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 🔹 Eliminar módulo
    fun eliminar(id: String, onComplete: (Boolean) -> Unit) {
        coleccion.document(id).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
