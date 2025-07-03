package com.example.blocodenotas

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class NotesListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var tvEmptyState: TextView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var repository: NotesRepository
    private var notesList = mutableListOf<Note>()

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 1001
        const val REQUEST_CODE_EDIT_NOTE = 1002
        private const val TAG = "NotesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)

        Log.d(TAG, "onCreate: NotesListActivity iniciada")

        initViews()
        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        observeNotes()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        Log.d(TAG, "initViews: Views inicializadas")
    }

    private fun setupDatabase() {
        val database = NotesDatabase.getDatabase(this)
        repository = NotesRepository(database.noteDao())
        Log.d(TAG, "setupDatabase: Database configurada")
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(notesList) { note, position ->
            showNoteOptions(note, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notesAdapter
        Log.d(TAG, "setupRecyclerView: RecyclerView configurado")
    }

    private fun setupClickListeners() {
        fabAddNote.setOnClickListener {
            Log.d(TAG, "FAB clicado - Abrindo NoteEditActivity")

            // Intent EXPLÍCITO para garantir que vai para a Activity correta
            val intent = Intent().apply {
                // Especifica explicitamente qual Activity abrir
                component = ComponentName(
                    "com.example.blocodenotas",
                    "com.example.blocodenotas.NoteEditActivity"
                )
                // Adiciona dados extras para debug
                putExtra("mode", "create")
                putExtra("debug", true)
            }

            Log.d(TAG, "Intent criado: ${intent.component}")

            try {
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                Log.d(TAG, "startActivityForResult chamado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao abrir NoteEditActivity", e)
                Toast.makeText(this, "Erro ao abrir editor: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeNotes() {
        lifecycleScope.launch {
            repository.getAllNotes().collect { notes ->
                Log.d(TAG, "observeNotes: ${notes.size} notas recebidas")
                notesList.clear()
                notesList.addAll(notes)
                notesAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
        }
    }

    private fun showNoteOptions(note: Note, position: Int) {
        val options = arrayOf("Abrir", "Excluir")

        AlertDialog.Builder(this)
            .setTitle(note.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openNote(note, position)
                    1 -> deleteNote(note, position)
                }
            }
            .show()
    }

    private fun openNote(note: Note, position: Int) {
        Log.d(TAG, "openNote: Abrindo nota ${note.id}")

        val intent = Intent().apply {
            component = ComponentName(
                "com.example.blocodenotas",
                "com.example.blocodenotas.NoteEditActivity"
            )
            putExtra("note_id", note.id)
            putExtra("note_title", note.title)
            putExtra("note_content", note.content)
            putExtra("note_position", position)
            putExtra("mode", "edit")
            putExtra("debug", true)
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE)
            Log.d(TAG, "Edit intent enviado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir nota para edição", e)
            Toast.makeText(this, "Erro ao abrir nota: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteNote(note: Note, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Nota")
            .setMessage("Tem certeza que deseja excluir esta nota?")
            .setPositiveButton("Sim") { _, _ ->
                lifecycleScope.launch {
                    try {
                        repository.deleteNote(note)
                        Toast.makeText(this@NotesListActivity, "Nota excluída", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Nota ${note.id} excluída")
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao excluir nota", e)
                        Toast.makeText(this@NotesListActivity, "Erro ao excluir nota", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun updateEmptyState() {
        if (notesList.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            Log.d(TAG, "updateEmptyState: Mostrando estado vazio")
        } else {
            tvEmptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            Log.d(TAG, "updateEmptyState: Mostrando lista com ${notesList.size} notas")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ADD_NOTE -> {
                    Log.d(TAG, "onActivityResult: Nova nota criada")
                    Toast.makeText(this, "Nota criada com sucesso", Toast.LENGTH_SHORT).show()
                }
                REQUEST_CODE_EDIT_NOTE -> {
                    Log.d(TAG, "onActivityResult: Nota editada")
                    Toast.makeText(this, "Nota atualizada com sucesso", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.d(TAG, "onActivityResult: Operação cancelada ou falhou")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: NotesListActivity voltou ao foco")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: NotesListActivity perdeu o foco")
    }
}