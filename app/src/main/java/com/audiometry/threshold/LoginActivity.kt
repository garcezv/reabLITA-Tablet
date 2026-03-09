package com.audiometry.threshold

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.audiometry.threshold.database.AppDatabase
import com.audiometry.threshold.database.SampleDataHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class LoginActivity : BaseActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDatabase.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            SampleDataHelper.populate(db)
        }

        if (prefs.getString("user_name", null) != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnAccess = findViewById<Button>(R.id.btnAccess)
        val btnForgotPassword = findViewById<Button>(R.id.btnForgotPassword)
        val btnRequestAccess = findViewById<Button>(R.id.btnRequestAccess)
        val btnGovLogin = findViewById<Button>(R.id.btnGovLogin)

        setupLanguageSelector(
            findViewById<LinearLayout>(R.id.llLanguageSelector),
            findViewById<TextView>(R.id.tvLanguageLabel)
        )

        btnAccess.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString()?.trim() ?: ""
            tilEmail.error = null
            tilPassword.error = null

            if (email.isEmpty()) { tilEmail.error = getString(R.string.error_empty_email); return@setOnClickListener }
            if (password.isEmpty()) { tilPassword.error = getString(R.string.error_empty_password); return@setOnClickListener }

            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) { db.userDao().login(email, password) }
                if (user != null) {
                    prefs.edit()
                        .putString("user_name", user.name)
                        .putString("user_role", user.role)
                        .putString("user_email", user.email)
                        .apply()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                } else {
                    tilPassword.error = getString(R.string.error_invalid_credentials)
                }
            }
        }

        btnForgotPassword.setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_forgot_password), Toast.LENGTH_SHORT).show()
        }

        btnRequestAccess.setOnClickListener {
            startActivity(Intent(this, SolicitarAcessoActivity::class.java))
        }

        btnGovLogin.setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_gov_login), Toast.LENGTH_SHORT).show()
        }
    }
}
