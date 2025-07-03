package com.example.blocodenotas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.launch

class NoteEditActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var toolbar: Toolbar
    private lateinit var repository: NotesRepository

    private var noteId: Long = -1
    private var notePosition: Int = -1
    private var isEditMode = false
    private var originalNote: Note? = null

    companion object {
        private const val TAG = "NoteEditActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        Log.d(TAG, "onCreate: NoteEditActivity iniciada")

        // Debug: verificar se é a Activity correta
        Log.d(TAG, "onCreate: Class = ${this.javaClass.name}")
        Log.d(TAG, "onCreate: Package = ${this.packageName}")

        initViews()
        setupDatabase()
        setupToolbar()
        loadNoteData()

        // Debug extra
        val mode = intent.getStringExtra("mode") ?: "unknown"
        val debug = intent.getBooleanExtra("debug", false)
        Log.d(TAG, "onCreate: Mode = $mode, Debug = $debug")
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etNoteTitle)
        etContent = findViewById(R.id.etNoteContent)
        toolbar = findViewById(R.id.toolbar)

        Log.d(TAG, "initViews: Views inicializadas")
    }

    private fun setupDatabase() {
        val database = NotesDatabase.getDatabase(this)
        repository = NotesRepository(database.noteDao())
        Log.d(TAG, "setupDatabase: Database configurada")
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        Log.d(TAG, "setupToolbar: Toolbar configurada")
    }

    private fun loadNoteData() {
        noteId = intent.getLongExtra("note_id", -1)
        notePosition = intent.getIntExtra("note_position", -1)

        Log.d(TAG, "loadNoteData: noteId = $noteId, notePosition = $notePosition")

        if (noteId != -1L) {
            // Modo edição
            isEditMode = true
            supportActionBar?.title = "Editar Nota"
            Log.d(TAG, "loadNoteData: Modo EDIÇÃO")

            lifecycleScope.launch {
                try {
                    originalNote = repository.getNoteById(noteId)
                    originalNote?.let { note ->
                        etTitle.setText(note.title)
                        etContent.setText(note.content)
                        Log.d(TAG, "loadNoteData: Nota carregada - ${note.title}")
                    } ?: run {
                        Log.e(TAG, "loadNoteData: Nota não encontrada")
                        Toast.makeText(this@NoteEditActivity, "Nota não encontrada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "loadNoteData: Erro ao carregar nota", e)
                    Toast.makeText(this@NoteEditActivity, "Erro ao carregar nota", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // Modo criação
            supportActionBar?.title = "Nova Nota"
            etTitle.requestFocus()
            Log.d(TAG, "loadNoteData: Modo CRIAÇÃO")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note_edit, menu)
        Log.d(TAG, "onCreateOptionsMenu: Menu criado")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item.itemId}")
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: Botão voltar pressionado")
                finish()
                true
            }
            R.id.action_save -> {
                Log.d(TAG, "onOptionsItemSelected: Botão salvar pressionado")
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        Log.d(TAG, "saveNote: title='$title', content length=${content.length}")

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "A nota está vazia", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (isEditMode && originalNote != null) {
                    // Atualizar nota existente
                    val updatedNote = originalNote!!.copy(
                        title = title.ifEmpty { "Nota sem título" },
                        content = content,
                        lastModified = System.currentTimeMillis()
                    )
                    repository.updateNote(updatedNote)
                    Log.d(TAG, "saveNote: Nota atualizada - ${updatedNote.title}")

                    val resultIntent = Intent().apply {
                        putExtra("note_id", noteId)
                        putExtra("title", updatedNote.title)
                        putExtra("content", updatedNote.content)
                        putExtra("action", "updated")
                    }
                    setResult(RESULT_OK, resultIntent)

                } else {
                    // Criar nova nota
                    val newNote = Note(
                        title = title.ifEmpty { "Nota sem título" },
                        content = content,
                        lastModified = System.currentTimeMillis()
                    )
                    val insertedId = repository.insertNote(newNote)
                    Log.d(TAG, "saveNote: Nova nota criada - ID=$insertedId, title='${newNote.title}'")

                    val resultIntent = Intent().apply {
                        putExtra("title", newNote.title)
                        putExtra("content", newNote.content)
                        putExtra("note_id", insertedId)
                        putExtra("action", "created")
                    }
                    setResult(RESULT_OK, resultIntent)
                }

                Toast.makeText(this@NoteEditActivity, "Nota salva", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                Log.e(TAG, "saveNote: Erro ao salvar nota", e)
                Toast.makeText(this@NoteEditActivity, "Erro ao salvar nota: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: Botão voltar do sistema pressionado")
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: NoteEditActivity destruída")
    }
}