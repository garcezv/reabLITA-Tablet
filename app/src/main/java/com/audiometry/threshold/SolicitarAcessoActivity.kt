package com.audiometry.threshold

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SolicitarAcessoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_acesso)

        setupLanguageSelector(
            findViewById<LinearLayout>(R.id.llLanguageSelector),
            findViewById<TextView>(R.id.tvLanguageLabel)
        )

        setupDropdowns()
        setupCepAutoFill()
        setupCpfValidation()

        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val tilCpf = findViewById<TextInputLayout>(R.id.tilCpf)
            val cpf = findViewById<TextInputEditText>(R.id.etCpf).text?.toString() ?: ""
            if (!CpfValidator.isValid(cpf)) {
                tilCpf.error = getString(R.string.cpf_invalid)
                return@setOnClickListener
            }
            tilCpf.error = null
            Toast.makeText(this, getString(R.string.msg_request_sent), Toast.LENGTH_LONG).show()
            finish()
        }

        findViewById<Button>(R.id.btnLearnMore).setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCpfValidation() {
        val tilCpf = findViewById<TextInputLayout>(R.id.tilCpf)
        val etCpf  = findViewById<TextInputEditText>(R.id.etCpf)
        etCpf.addTextChangedListener(CpfMaskWatcher(etCpf, tilCpf, this))
    }

    private fun setupDropdowns() {
        val stateOptions = listOf("AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS",
            "MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO")
        val accessTypes = listOf("Administrador", "Facilitador", "Pesquisador")

        val actvState = findViewById<AutoCompleteTextView>(R.id.actvState)
        actvState.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stateOptions))

        val actvAccess = findViewById<AutoCompleteTextView>(R.id.actvAccessType)
        actvAccess.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, accessTypes))
    }

    private fun setupCepAutoFill() {
        val tilCep    = findViewById<TextInputLayout>(R.id.tilCep)
        val etCep     = findViewById<TextInputEditText>(R.id.etCep)
        val etStreet  = findViewById<TextInputEditText>(R.id.etStreet)
        val etNeigh   = findViewById<TextInputEditText>(R.id.etNeighborhood)
        val etCity    = findViewById<TextInputEditText>(R.id.etCity)
        val actvState = findViewById<AutoCompleteTextView>(R.id.actvState)

        etCep.addTextChangedListener { editable ->
            val digits = editable?.toString()?.replace(Regex("[^0-9]"), "") ?: ""
            tilCep.error = null
            if (digits.length == 8) {
                tilCep.helperText = getString(R.string.cep_searching)
                lifecycleScope.launch {
                    val addr = ViaCepService.fetch(digits)
                    tilCep.helperText = null
                    if (addr != null) {
                        etStreet.setText(addr.logradouro)
                        etNeigh.setText(addr.bairro)
                        etCity.setText(addr.localidade)
                        actvState.setText(addr.uf, false)
                    } else {
                        tilCep.error = getString(R.string.cep_not_found)
                    }
                }
            } else {
                tilCep.helperText = null
            }
        }
    }
}
