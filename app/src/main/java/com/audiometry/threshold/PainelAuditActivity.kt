package com.audiometry.threshold

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audiometry.threshold.adapter.ConfirmParticipanteAdapter
import com.audiometry.threshold.adapter.ParticipanteAdapter
import com.audiometry.threshold.database.AppDatabase
import com.audiometry.threshold.database.entity.Participant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PainelAuditActivity : BaseActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ParticipanteAdapter
    private var allParticipants: List<Participant> = emptyList()
    private var filteredParticipants: List<Participant> = emptyList()
    private var selectedParticipants: List<Participant> = emptyList()

    private val pageSize = 10
    private var currentPage = 0
    private var searchJob: Job? = null

    private lateinit var tvSelectedCount: TextView
    private lateinit var tvShowingItems: TextView
    private lateinit var tvCurrentPage: TextView
    private lateinit var cbSelectAll: CheckBox
    private lateinit var actvStatus: AutoCompleteTextView
    private lateinit var actvReeval: AutoCompleteTextView
    private lateinit var actvTestDone: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_painel_audit)

        db = AppDatabase.getInstance(this)

        setupLanguageSelector(
            findViewById<LinearLayout>(R.id.llLanguageSelector),
            findViewById<TextView>(R.id.tvLanguageLabel)
        )

        tvSelectedCount = findViewById(R.id.tvSelectedCount)
        tvShowingItems = findViewById(R.id.tvShowingItems)
        tvCurrentPage = findViewById(R.id.tvCurrentPage)
        cbSelectAll = findViewById(R.id.cbSelectAll)
        actvStatus = findViewById(R.id.actvFilterStatus)
        actvReeval = findViewById(R.id.actvFilterReeval)
        actvTestDone = findViewById(R.id.actvFilterTestDone)

        setupDropdowns()
        setupRecyclerView()
        setupButtons()
        loadParticipants()
    }

    private fun setupDropdowns() {
        val statusOptions = listOf(
            getString(R.string.filter_all), getString(R.string.filter_normal), getString(R.string.filter_alterado)
        )
        val reevalOptions = listOf(
            getString(R.string.filter_all), getString(R.string.filter_sim), getString(R.string.filter_nao)
        )
        val testOptions = listOf(
            getString(R.string.filter_all), getString(R.string.filter_audit), getString(R.string.filter_ouvir)
        )

        actvStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusOptions))
        actvReeval.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, reevalOptions))
        actvTestDone.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, testOptions))
    }

    private fun setupRecyclerView() {
        adapter = ParticipanteAdapter(
            onSelectionChanged = { selected ->
                selectedParticipants = selected
                tvSelectedCount.text = getString(R.string.selected_count, selected.size)
                cbSelectAll.setOnCheckedChangeListener(null)
                cbSelectAll.isChecked = adapter.isAllSelected()
                cbSelectAll.setOnCheckedChangeListener { _, checked -> adapter.selectAll(checked) }
            },
            onActionClick = { participant, view -> showActionsMenu(participant, view) }
        )

        val rv = findViewById<RecyclerView>(R.id.rvParticipants)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        cbSelectAll.setOnCheckedChangeListener { _, checked -> adapter.selectAll(checked) }
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            openSideMenu()
        }
        findViewById<Button>(R.id.btnLearnMore).setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnInstructions).setOnClickListener {
            startActivity(Intent(this, InstrucoesActivity::class.java))
        }
        findViewById<Button>(R.id.btnCheckNoise).setOnClickListener {
            startActivity(Intent(this, ChecarRuidoActivity::class.java))
        }
        findViewById<Button>(R.id.btnRegisterParticipant).setOnClickListener {
            startActivityForResult(Intent(this, CadastroParticipanteActivity::class.java), REQ_REGISTER)
        }

        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    applySearchFilter(s?.toString() ?: "")
                }
            }
        })

        findViewById<Button>(R.id.btnApplyFilters).setOnClickListener { applyDropdownFilters() }

        findViewById<ImageButton>(R.id.btnDeleteSelected).setOnClickListener { deleteSelected() }
        findViewById<ImageButton>(R.id.btnFirstPage).setOnClickListener { goToPage(0) }
        findViewById<ImageButton>(R.id.btnPrevPage).setOnClickListener { goToPage(currentPage - 1) }
        findViewById<ImageButton>(R.id.btnNextPage).setOnClickListener { goToPage(currentPage + 1) }
        findViewById<ImageButton>(R.id.btnLastPage).setOnClickListener { goToPage(totalPages() - 1) }

        findViewById<Button>(R.id.btnStartTest).setOnClickListener { onStartTestClicked() }
    }

    private fun loadParticipants() {
        lifecycleScope.launch {
            allParticipants = withContext(Dispatchers.IO) { db.participantDao().getAll() }
            filteredParticipants = allParticipants
            currentPage = 0
            updateDisplay()
        }
    }

    private fun applySearchFilter(query: String) {
        lifecycleScope.launch {
            filteredParticipants = if (query.isBlank()) {
                allParticipants
            } else {
                withContext(Dispatchers.IO) { db.participantDao().search(query) }
            }
            currentPage = 0
            updateDisplay()
        }
    }

    private fun applyDropdownFilters() {
        val all = getString(R.string.filter_all)
        val status = actvStatus.text.toString().let { if (it == all || it.isBlank()) "" else it }
        val reeval = actvReeval.text.toString().let { if (it == all || it.isBlank()) "" else it }
        val testType = actvTestDone.text.toString().let {
            when {
                it == all || it.isBlank() -> ""
                it == getString(R.string.filter_audit) -> "Aud.IT"
                it == getString(R.string.filter_ouvir) -> "Ouvir Brasil"
                else -> it
            }
        }
        lifecycleScope.launch {
            filteredParticipants = withContext(Dispatchers.IO) {
                db.participantDao().filter(status, reeval, testType)
            }
            currentPage = 0
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        val total = filteredParticipants.size
        val totalPgs = totalPages()
        val safeCurrentPage = currentPage.coerceIn(0, (totalPgs - 1).coerceAtLeast(0))
        currentPage = safeCurrentPage
        val start = safeCurrentPage * pageSize
        val end = (start + pageSize).coerceAtMost(total)
        val page = if (total == 0) emptyList() else filteredParticipants.subList(start, end)

        adapter.submitList(page)
        tvShowingItems.text = getString(R.string.showing_items, total, allParticipants.size)
        tvCurrentPage.text = "${String.format("%02d", safeCurrentPage + 1)} / ${totalPgs.coerceAtLeast(1)}"
        updateNavButtons(safeCurrentPage, totalPgs)
    }

    private fun updateNavButtons(page: Int, total: Int) {
        val firstBtn = findViewById<ImageButton>(R.id.btnFirstPage)
        val prevBtn = findViewById<ImageButton>(R.id.btnPrevPage)
        val nextBtn = findViewById<ImageButton>(R.id.btnNextPage)
        val lastBtn = findViewById<ImageButton>(R.id.btnLastPage)
        firstBtn.isEnabled = page > 0
        prevBtn.isEnabled = page > 0
        nextBtn.isEnabled = page < total - 1
        lastBtn.isEnabled = page < total - 1
    }

    private fun goToPage(page: Int) {
        val total = totalPages()
        currentPage = page.coerceIn(0, (total - 1).coerceAtLeast(0))
        updateDisplay()
    }

    private fun totalPages() = if (filteredParticipants.isEmpty()) 1
        else (filteredParticipants.size + pageSize - 1) / pageSize

    private fun onStartTestClicked() {
        val selected = adapter.getSelected()
        if (selected.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_participants_selected), Toast.LENGTH_SHORT).show()
            return
        }
        showConfirmParticipantsDialog(selected)
    }

    private fun showConfirmParticipantsDialog(selected: List<Participant>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_participants, null)
        val rvConfirm = dialogView.findViewById<RecyclerView>(R.id.rvConfirmList)
        rvConfirm.layoutManager = LinearLayoutManager(this)
        val confirmAdapter = ConfirmParticipanteAdapter(selected.toMutableList()) { _ ->
            // participant removed from list — no extra action needed
        }
        rvConfirm.adapter = confirmAdapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnStartNow).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }
        dialogView.findViewById<Button>(R.id.btnCancelTest).setOnClickListener {
            dialog.dismiss()
            showCancelChangesDialog()
        }

        dialog.show()
    }

    private fun showCancelChangesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_warning, null)
        dialogView.findViewById<TextView>(R.id.tvWarningTitle).text =
            getString(R.string.cancel_changes_title)
        dialogView.findViewById<TextView>(R.id.tvWarningMessage).text =
            getString(R.string.cancel_changes_message)
        dialogView.findViewById<Button>(R.id.btnWarningConfirm).text =
            getString(R.string.btn_yes_cancel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnWarningBack).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnWarningConfirm).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showActionsMenu(participant: Participant, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 0, 0, "Editar")
        popup.menu.add(0, 1, 1, "Excluir")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
                1 -> confirmDelete(participant)
            }
            true
        }
        popup.show()
    }

    private fun confirmDelete(participant: Participant) {
        AlertDialog.Builder(this)
            .setTitle("Excluir participante")
            .setMessage("Deseja excluir ${participant.nomeCompleto}?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { db.participantDao().delete(participant) }
                    loadParticipants()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteSelected() {
        val toDelete = adapter.getSelected()
        if (toDelete.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("Excluir selecionados")
            .setMessage("Excluir ${toDelete.size} participante(s)?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { toDelete.forEach { db.participantDao().delete(it) } }
                    loadParticipants()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_REGISTER && resultCode == RESULT_OK) loadParticipants()
    }

    companion object {
        const val REQ_REGISTER = 1001
    }
}
