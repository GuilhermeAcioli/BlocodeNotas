package com.example.blocodenotas

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREF_NAME = "NotesApp"
        private const val KEY_PASSWORD = "password"
        private const val DEFAULT_PASSWORD = "1234"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSharedPreferences()
        setupClickListeners()
    }

    private fun initViews() {
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Define senha padrão se não existir
        if (!sharedPreferences.contains(KEY_PASSWORD)) {
            sharedPreferences.edit()
                .putString(KEY_PASSWORD, DEFAULT_PASSWORD)
                .apply()
        }
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val enteredPassword = etPassword.text.toString()
            val savedPassword = sharedPreferences.getString(KEY_PASSWORD, DEFAULT_PASSWORD)

            if (enteredPassword == savedPassword) {
                val intent = Intent(this, NotesListActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                etPassword.text.clear()
            }
        }
    }
}