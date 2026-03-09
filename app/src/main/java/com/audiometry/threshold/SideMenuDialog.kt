package com.audiometry.threshold

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class SideMenuDialog(private val activity: Activity) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_side_menu)

        window?.apply {
            setGravity(Gravity.START or Gravity.TOP)
            setLayout(
                (context.resources.displayMetrics.widthPixels * 0.42).toInt(),
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setWindowAnimations(R.style.SideMenuAnimation)
            setBackgroundDrawableResource(android.R.color.transparent)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.also { it.dimAmount = 0.5f }
        }

        val prefs = activity.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Usuário") ?: "Usuário"
        val userRole = prefs.getString("user_role", activity.getString(R.string.label_facilitador))
            ?: activity.getString(R.string.label_facilitador)

        val initials = userName.trim().split("\\s+".toRegex())
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "U" }

        findViewById<TextView>(R.id.tvUserInitials).text = initials
        findViewById<TextView>(R.id.tvUserName).text = userName
        findViewById<TextView>(R.id.tvUserRole).text = userRole

        findViewById<ImageButton>(R.id.btnCloseMenu).setOnClickListener { dismiss() }

        setupItem(R.id.llMenuInstrucoes) {
            dismiss()
            activity.startActivity(Intent(activity, InstrucoesActivity::class.java))
        }

        setupItem(R.id.llMenuChecarRuido) {
            dismiss()
            activity.startActivity(Intent(activity, ChecarRuidoActivity::class.java))
        }

        setupItem(R.id.llMenuProtocolo) {
            dismiss()
            Toast.makeText(activity, activity.getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        setupItem(R.id.llMenuCalibracao) {
            dismiss()
            Toast.makeText(activity, activity.getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        setupItem(R.id.llMenuGerenciarParticipantes) {
            dismiss()
            activity.startActivity(Intent(activity, PainelAuditActivity::class.java))
        }

        setupItem(R.id.llMenuGerenciarFacilitadores) {
            dismiss()
            Toast.makeText(activity, activity.getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        setupItem(R.id.llMenuEditarPerfil) {
            dismiss()
            Toast.makeText(activity, activity.getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        setupItem(R.id.llMenuSair) {
            dismiss()
            prefs.edit()
                .remove("user_name")
                .remove("user_role")
                .remove("user_email")
                .apply()
            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
        }
    }

    private fun setupItem(id: Int, action: () -> Unit) {
        try {
            findViewById<LinearLayout>(id)?.setOnClickListener { action() }
        } catch (_: Exception) { }
    }
}
