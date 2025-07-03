package com.example.blocodenotas

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotesListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var notesList = mutableListOf<Note>()

    companion object {
        private const val PREF_NAME = "NotesApp"
        private const val KEY_NOTES = "notes_list"
        const val REQUEST_CODE_ADD_NOTE = 1001
        const val REQUEST_CODE_EDIT_NOTE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)

        initViews()
        setupRecyclerView()
        loadNotes()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(notesList) { note, position ->
            showNoteOptions(note, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notesAdapter
    }

    private fun setupClickListeners() {
        fabAddNote.setOnClickListener {
            val intent = Intent(this, NoteEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }
    }

    private fun showNoteOptions(note: Note, position: Int) {
        val options = arrayOf("Abrir", "Excluir")

        AlertDialog.Builder(this)
            .setTitle(note.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openNote(note, position)
                    1 -> deleteNote(position)
                }
            }
            .show()
    }

    private fun openNote(note: Note, position: Int) {
        val intent = Intent(this, NoteEditActivity::class.java).apply {
            putExtra("note_id", note.id)
            putExtra("note_title", note.title)
            putExtra("note_content", note.content)
            putExtra("note_position", position)
        }
        startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE)
    }

    private fun deleteNote(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Nota")
            .setMessage("Tem certeza que deseja excluir esta nota?")
            .setPositiveButton("Sim") { _, _ ->
                notesList.removeAt(position)
                notesAdapter.notifyItemRemoved(position)
                saveNotes()
                Toast.makeText(this, "Nota excluída", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun loadNotes() {
        val notesJson = sharedPreferences.getString(KEY_NOTES, "[]")
        val type = object : TypeToken<MutableList<Note>>() {}.type
        notesList.clear()
        notesList.addAll(Gson().fromJson(notesJson, type))
        notesAdapter.notifyDataSetChanged()
    }

    private fun saveNotes() {
        val notesJson = Gson().toJson(notesList)
        sharedPreferences.edit()
            .putString(KEY_NOTES, notesJson)
            .apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_ADD_NOTE -> {
                    val title = data.getStringExtra("title") ?: ""
                    val content = data.getStringExtra("content") ?: ""

                    if (title.isNotEmpty() || content.isNotEmpty()) {
                        val newNote = Note(
                            id = System.currentTimeMillis(),
                            title = title.ifEmpty { "Nota sem título" },
                            content = content,
                            lastModified = System.currentTimeMillis()
                        )

                        notesList.add(0, newNote)
                        notesAdapter.notifyItemInserted(0)
                        recyclerView.scrollToPosition(0)
                        saveNotes()
                    }
                }

                REQUEST_CODE_EDIT_NOTE -> {
                    val position = data.getIntExtra("note_position", -1)
                    val title = data.getStringExtra("title") ?: ""
                    val content = data.getStringExtra("content") ?: ""

                    if (position >= 0 && position < notesList.size) {
                        notesList[position].apply {
                            this.title = title.ifEmpty { "Nota sem título" }
                            this.content = content
                            this.lastModified = System.currentTimeMillis()
                        }

                        notesAdapter.notifyItemChanged(position)
                        saveNotes()
                    }
                }
            }
        }
    }
}