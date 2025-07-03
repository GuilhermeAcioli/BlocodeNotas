package com.example.blocodenotas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private val notes: MutableList<Note>,
    private val onNoteClick: (Note, Int) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvNoteTitle)
        val contentTextView: TextView = itemView.findViewById(R.id.tvNoteContent)
        val dateTextView: TextView = itemView.findViewById(R.id.tvNoteDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        holder.titleTextView.text = note.title
        holder.contentTextView.text = if (note.content.length > 100) {
            note.content.substring(0, 100) + "..."
        } else {
            note.content
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(Date(note.lastModified))

        holder.itemView.setOnClickListener {
            onNoteClick(note, position)
        }
    }

    override fun getItemCount(): Int = notes.size
}