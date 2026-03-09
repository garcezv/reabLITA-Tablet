package com.audiometry.threshold

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
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

class SelectParticipantsDialog : DialogFragment() {

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

    private val launchCadastro = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) loadParticipants()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_select_participants, container, false)

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val w = (resources.displayMetrics.widthPixels * 0.92).toInt()
            val h = (resources.displayMetrics.heightPixels * 0.90).toInt()
            setLayout(w, h)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        bindViews(view)
        setupDropdowns()
        setupRecyclerView(view)
        setupButtons(view)
        loadParticipants()
    }

    private fun bindViews(view: View) {
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount)
        tvShowingItems  = view.findViewById(R.id.tvShowingItems)
        tvCurrentPage   = view.findViewById(R.id.tvCurrentPage)
        cbSelectAll     = view.findViewById(R.id.cbSelectAll)
        actvStatus      = view.findViewById(R.id.actvFilterStatus)
        actvReeval      = view.findViewById(R.id.actvFilterReeval)
        actvTestDone    = view.findViewById(R.id.actvFilterTestDone)
    }

    private fun setupDropdowns() {
        val ctx = requireContext()
        val statusOpts = listOf(getString(R.string.filter_all), getString(R.string.filter_normal), getString(R.string.filter_alterado))
        val reevalOpts  = listOf(getString(R.string.filter_all), getString(R.string.filter_sim),   getString(R.string.filter_nao))
        val testOpts    = listOf(getString(R.string.filter_all), getString(R.string.filter_audit),  getString(R.string.filter_ouvir))
        actvStatus.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, statusOpts))
        actvReeval.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, reevalOpts))
        actvTestDone.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, testOpts))
    }

    private fun setupRecyclerView(view: View) {
        adapter = ParticipanteAdapter(
            onSelectionChanged = { selected ->
                selectedParticipants = selected
                tvSelectedCount.text = getString(R.string.selected_count, selected.size)
                cbSelectAll.setOnCheckedChangeListener(null)
                cbSelectAll.isChecked = adapter.isAllSelected()
                cbSelectAll.setOnCheckedChangeListener { _, checked -> adapter.selectAll(checked) }
            },
            onActionClick = { participant, anchor -> showActionsMenu(participant, anchor) }
        )
        view.findViewById<RecyclerView>(R.id.rvParticipants).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectParticipantsDialog.adapter
        }
        cbSelectAll.setOnCheckedChangeListener { _, checked -> adapter.selectAll(checked) }
    }

    private fun setupButtons(view: View) {
        view.findViewById<ImageButton>(R.id.btnCloseDialog).setOnClickListener { dismiss() }

        view.findViewById<Button>(R.id.btnRegisterParticipant).setOnClickListener {
            launchCadastro.launch(Intent(requireContext(), CadastroParticipanteActivity::class.java))
        }

        view.findViewById<android.widget.EditText>(R.id.etSearch)
            .addTextChangedListener(object : TextWatcher {
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

        view.findViewById<Button>(R.id.btnApplyFilters).setOnClickListener { applyDropdownFilters() }

        view.findViewById<ImageButton>(R.id.btnDeleteSelected).setOnClickListener { deleteSelected() }
        view.findViewById<ImageButton>(R.id.btnFirstPage).setOnClickListener { goToPage(0) }
        view.findViewById<ImageButton>(R.id.btnPrevPage).setOnClickListener { goToPage(currentPage - 1) }
        view.findViewById<ImageButton>(R.id.btnNextPage).setOnClickListener { goToPage(currentPage + 1) }
        view.findViewById<ImageButton>(R.id.btnLastPage).setOnClickListener { goToPage(totalPages() - 1) }

        view.findViewById<Button>(R.id.btnStartTest).setOnClickListener { onStartTestClicked() }
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
            filteredParticipants = if (query.isBlank()) allParticipants
            else withContext(Dispatchers.IO) { db.participantDao().search(query) }
            currentPage = 0
            updateDisplay()
        }
    }

    private fun applyDropdownFilters() {
        val all = getString(R.string.filter_all)
        val status   = actvStatus.text.toString().let { if (it == all || it.isBlank()) "" else it }
        val reeval   = actvReeval.text.toString().let { if (it == all || it.isBlank()) "" else it }
        val testType = actvTestDone.text.toString().let {
            when { it == all || it.isBlank() -> ""; it == getString(R.string.filter_audit) -> "Aud.IT"
                   it == getString(R.string.filter_ouvir) -> "Ouvir Brasil"; else -> it }
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
        val total     = filteredParticipants.size
        val totalPgs  = totalPages()
        val safePage  = currentPage.coerceIn(0, (totalPgs - 1).coerceAtLeast(0))
        currentPage   = safePage
        val start = safePage * pageSize
        val end   = (start + pageSize).coerceAtMost(total)
        val page  = if (total == 0) emptyList() else filteredParticipants.subList(start, end)

        adapter.submitList(page)
        tvShowingItems.text = getString(R.string.showing_items, total, allParticipants.size)
        tvCurrentPage.text  = "${String.format("%02d", safePage + 1)} / ${totalPgs.coerceAtLeast(1)}"

        view?.let { v ->
            v.findViewById<ImageButton>(R.id.btnFirstPage).isEnabled = safePage > 0
            v.findViewById<ImageButton>(R.id.btnPrevPage).isEnabled  = safePage > 0
            v.findViewById<ImageButton>(R.id.btnNextPage).isEnabled  = safePage < totalPgs - 1
            v.findViewById<ImageButton>(R.id.btnLastPage).isEnabled  = safePage < totalPgs - 1
        }
    }

    private fun goToPage(page: Int) {
        currentPage = page.coerceIn(0, (totalPages() - 1).coerceAtLeast(0))
        updateDisplay()
    }

    private fun totalPages() =
        if (filteredParticipants.isEmpty()) 1
        else (filteredParticipants.size + pageSize - 1) / pageSize

    private fun onStartTestClicked() {
        val selected = adapter.getSelected()
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_participants_selected), Toast.LENGTH_SHORT).show()
            return
        }
        showConfirmParticipantsDialog(selected)
    }

    private fun showConfirmParticipantsDialog(selected: List<Participant>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_participants, null)
        val rvConfirm  = dialogView.findViewById<RecyclerView>(R.id.rvConfirmList)
        rvConfirm.layoutManager = LinearLayoutManager(requireContext())
        rvConfirm.adapter = ConfirmParticipanteAdapter(selected.toMutableList()) {}

        val confirmDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnStartNow).setOnClickListener {
            confirmDialog.dismiss()
            dismiss()
            Toast.makeText(requireContext(), getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }
        dialogView.findViewById<Button>(R.id.btnCancelTest).setOnClickListener {
            confirmDialog.dismiss()
            showCancelChangesDialog()
        }
        confirmDialog.show()
    }

    private fun showCancelChangesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_warning, null)
        dialogView.findViewById<TextView>(R.id.tvWarningTitle).text  = getString(R.string.cancel_changes_title)
        dialogView.findViewById<TextView>(R.id.tvWarningMessage).text = getString(R.string.cancel_changes_message)
        dialogView.findViewById<Button>(R.id.btnWarningConfirm).text  = getString(R.string.btn_yes_cancel)

        val warningDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        warningDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnWarningBack).setOnClickListener { warningDialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnWarningConfirm).setOnClickListener {
            warningDialog.dismiss()
            dismiss()
        }
        warningDialog.show()
    }

    private fun showActionsMenu(participant: Participant, anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 0, 0, "Editar")
        popup.menu.add(0, 1, 1, "Excluir")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> Toast.makeText(requireContext(), getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
                1 -> confirmDelete(participant)
            }
            true
        }
        popup.show()
    }

    private fun confirmDelete(participant: Participant) {
        AlertDialog.Builder(requireContext())
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
        AlertDialog.Builder(requireContext())
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
}
