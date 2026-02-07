package com.cibertec.ecotrujilloapp.modulo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ecotrujilloapp.R

class ModuloAdapter(
    private val lista: List<Modulo>,
    private val onItemClick: (Modulo) -> Unit
) : RecyclerView.Adapter<ModuloAdapter.ModuloViewHolder>() {

    inner class ModuloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivModulo: ImageView = itemView.findViewById(R.id.imgModulo)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreModulo)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionModulo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_modulo, parent, false)
        return ModuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuloViewHolder, position: Int) {
        val modulo = lista[position]
        holder.tvNombre.text = modulo.nombre
        holder.tvDescripcion.text = modulo.descripcion

        // Cargar imagen del drawable según nombre guardado en Firebase
        val context = holder.itemView.context
        val imageResId = context.resources.getIdentifier(
            modulo.imagenUrl, "drawable", context.packageName
        )
        if (imageResId != 0) holder.ivModulo.setImageResource(imageResId)

        holder.itemView.setOnClickListener {
            onItemClick(modulo)
        }
    }

    override fun getItemCount() = lista.size
}
