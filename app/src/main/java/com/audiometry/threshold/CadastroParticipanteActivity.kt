package com.audiometry.threshold

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.audiometry.threshold.database.AppDatabase
import com.audiometry.threshold.database.entity.Participant
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroParticipanteActivity : BaseActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_participante)

        db = AppDatabase.getInstance(this)

        setupDropdowns()
        setupCepAutoFill()
        setupCpfValidation()

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            showCancelDialog()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            showCancelDialog()
        }

        findViewById<Button>(R.id.btnFinish).setOnClickListener {
            onFinishClicked()
        }

        findViewById<LinearLayout>(R.id.llAddFile).setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCpfValidation() {
        val tilCpf = findViewById<TextInputLayout>(R.id.tilCpf)
        val etCpf  = findViewById<TextInputEditText>(R.id.etCpf)
        etCpf.addTextChangedListener(CpfMaskWatcher(etCpf, tilCpf, this))
    }

    private fun setupCepAutoFill() {
        val tilCep   = findViewById<TextInputLayout>(R.id.tilCep)
        val etCep    = findViewById<TextInputEditText>(R.id.etCep)
        val etStreet = findViewById<TextInputEditText>(R.id.etStreet)
        val etNeigh  = findViewById<TextInputEditText>(R.id.etNeighborhood)
        val etCity   = findViewById<TextInputEditText>(R.id.etCity)
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

    private fun setupDropdowns() {
        val bioSexOptions = listOf("Masculino", "Feminino", "Intersexo")
        val genderOptions = listOf("Homem cisgênero", "Mulher cisgênero", "Homem transgênero",
            "Mulher transgênero", "Não-binário", "Prefiro não informar")
        val raceOptions = listOf("Branco", "Preto", "Pardo", "Amarelo", "Indígena", "Prefiro não informar")
        val hearingOptions = listOf("Sim", "Não")
        val stateOptions = listOf("AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS",
            "MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO")

        setDropdown(R.id.actvBioSex, bioSexOptions)
        setDropdown(R.id.actvGender, genderOptions)
        setDropdown(R.id.actvRaceColor, raceOptions)
        setDropdown(R.id.actvHearingComplaint, hearingOptions)
        setDropdown(R.id.actvState, stateOptions)

        val actvHearing = findViewById<AutoCompleteTextView>(R.id.actvHearingComplaint)
        val etDetails = findViewById<TextInputEditText>(R.id.etComplaintDetails)
        actvHearing.setOnItemClickListener { _, _, position, _ ->
            etDetails.isEnabled = hearingOptions[position] == "Sim"
        }
    }

    private fun setDropdown(viewId: Int, options: List<String>) {
        val actv = findViewById<AutoCompleteTextView>(viewId)
        actv.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options))
    }

    private fun onFinishClicked() {
        val etCpf = findViewById<TextInputEditText>(R.id.etCpf)
        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etBirthDate = findViewById<TextInputEditText>(R.id.etBirthDate)
        val etMothersName = findViewById<TextInputEditText>(R.id.etMothersName)
        val etInstitution = findViewById<TextInputEditText>(R.id.etInstitution)

        val cpf = etCpf.text?.toString()?.trim() ?: ""
        val nome = etFullName.text?.toString()?.trim() ?: ""
        val nascimento = etBirthDate.text?.toString()?.trim() ?: ""
        val nomeMae = etMothersName.text?.toString()?.trim() ?: ""
        val instituicao = etInstitution.text?.toString()?.trim() ?: ""

        if (cpf.isEmpty() || nome.isEmpty() || nascimento.isEmpty() || nomeMae.isEmpty() || instituicao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios (●)", Toast.LENGTH_SHORT).show()
            return
        }

        if (!CpfValidator.isValid(cpf)) {
            findViewById<TextInputLayout>(R.id.tilCpf).error = getString(R.string.cpf_invalid)
            return
        }

        val participant = Participant(
            cpf = cpf,
            nomeCompleto = nome,
            dataNascimento = nascimento,
            nomeSocial = getFieldText(R.id.etSocialName),
            rg = getFieldText(R.id.etRg),
            orgaoExpedidor = getFieldText(R.id.etIssuingAuthority),
            dataExpedicao = getFieldText(R.id.etIssueDate),
            sexoBiologico = getDropdown(R.id.actvBioSex),
            identidadeGenero = getDropdown(R.id.actvGender),
            racaCor = getDropdown(R.id.actvRaceColor),
            nomeMae = nomeMae,
            nomePai = getFieldText(R.id.etFathersName),
            emailPessoal = getFieldText(R.id.etPersonalEmail),
            telefone = getFieldText(R.id.etPhone),
            cep = getFieldText(R.id.etCep),
            estado = getDropdown(R.id.actvState),
            municipio = getFieldText(R.id.etCity),
            bairro = getFieldText(R.id.etNeighborhood),
            logradouro = getFieldText(R.id.etStreet),
            numero = getFieldText(R.id.etNumber),
            complemento = getFieldText(R.id.etComplement),
            instituicao = instituicao,
            professorResponsavel = getFieldText(R.id.etTeacher),
            queixaAuditiva = getDropdown(R.id.actvHearingComplaint).ifEmpty { "Não" },
            detalhesQueixa = getFieldText(R.id.etComplaintDetails),
            outrasObservacoes = getFieldText(R.id.etOtherObs)
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.participantDao().insert(participant) }
            Toast.makeText(this@CadastroParticipanteActivity,
                getString(R.string.msg_registration_saved), Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun getFieldText(viewId: Int): String =
        (findViewById<TextInputEditText>(viewId))?.text?.toString()?.trim() ?: ""

    private fun getDropdown(viewId: Int): String =
        (findViewById<AutoCompleteTextView>(viewId))?.text?.toString()?.trim() ?: ""

    private fun showCancelDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_warning, null)
        dialogView.findViewById<TextView>(R.id.tvWarningTitle).text =
            getString(R.string.cancel_registration_title)
        dialogView.findViewById<TextView>(R.id.tvWarningMessage).text =
            getString(R.string.cancel_registration_message)
        dialogView.findViewById<Button>(R.id.btnWarningConfirm).text =
            getString(R.string.btn_yes_cancel_reg)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnWarningBack).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnWarningConfirm).setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
