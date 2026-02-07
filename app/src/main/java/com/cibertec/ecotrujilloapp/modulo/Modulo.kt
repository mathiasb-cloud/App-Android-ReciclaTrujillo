package com.cibertec.ecotrujilloapp.modulo

data class Modulo(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var latitud: Double? = null,
    var longitud: Double? = null,
    var imagenUrl: String = ""
) {
    fun getLatitudFinal(): Double = latitud ?: 0.0
    fun getLongitudFinal(): Double = longitud ?: 0.0
}
