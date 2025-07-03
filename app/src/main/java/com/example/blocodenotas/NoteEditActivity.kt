package com.example.blocodenotas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class NoteEditActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var toolbar: Toolbar

    private var noteId: Long = -1
    private var notePosition: Int = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        initViews()
        setupToolbar()
        loadNoteData()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etNoteTitle)
        etContent = findViewById(R.id.etNoteContent)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
    }

    private fun loadNoteData() {
        noteId = intent.getLongExtra("note_id", -1)
        notePosition = intent.getIntExtra("note_position", -1)

        if (noteId != -1L) {
            // Modo edição
            isEditMode = true
            val title = intent.getStringExtra("note_title") ?: ""
            val content = intent.getStringExtra("note_content") ?: ""

            etTitle.setText(title)
            etContent.setText(content)

            supportActionBar?.title = "Editar Nota"
        } else {
            // Modo criação
            supportActionBar?.title = "Nova Nota"
            etTitle.requestFocus()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "A nota está vazia", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra("title", title)
            putExtra("content", content)
            if (isEditMode) {
                putExtra("note_position", notePosition)
            }
        }

        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, "Nota salva", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}