package com.streaming.platform.ui.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.streaming.platform.R
import com.streaming.platform.models.Content

class ContentAdapter(
    private val contentList: List<Content>,
    private val isAdmin: Boolean,
    private val onItemClick: (Content) -> Unit,
    private val onEditClick: (Content) -> Unit,
    private val onDeleteClick: (Content) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val btnEdit: View? = view.findViewById(R.id.btnEdit)
        val btnDelete: View? = view.findViewById(R.id.btnDelete)
        val layoutActions: View? = view.findViewById(R.id.layoutActions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = contentList[position]
        holder.tvTitle.text = content.title
        holder.tvDescription.text = content.description
        
        holder.itemView.setOnClickListener { onItemClick(content) }
        
        if (isAdmin) {
            holder.layoutActions?.visibility = View.VISIBLE
            holder.btnEdit?.setOnClickListener { onEditClick(content) }
            holder.btnDelete?.setOnClickListener { onDeleteClick(content) }
        } else {
            holder.layoutActions?.visibility = View.GONE
        }
    }

    override fun getItemCount() = contentList.size
}
