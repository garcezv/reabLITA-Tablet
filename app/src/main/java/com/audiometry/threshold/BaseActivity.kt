package com.audiometry.threshold

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    protected lateinit var prefs: SharedPreferences
    private var createdWithLang: String = "pt"

    override fun attachBaseContext(newBase: Context) {
        val savedPrefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = savedPrefs.getString("language", "pt") ?: "pt"
        super.attachBaseContext(wrapLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        createdWithLang = prefs.getString("language", "pt") ?: "pt"
    }

    override fun onResume() {
        super.onResume()
        val currentLang = prefs.getString("language", "pt") ?: "pt"
        if (currentLang != createdWithLang) {
            recreate()
        }
    }

    private fun wrapLocale(context: Context, lang: String): Context {
        val locale = when (lang) {
            "en" -> Locale.ENGLISH
            "es" -> Locale("es")
            else -> Locale("pt", "BR")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    protected fun openSideMenu() {
        SideMenuDialog(this).show()
    }

    protected fun setupLanguageSelector(llSelector: LinearLayout?, tvLabel: TextView?) {
        val savedLang = prefs.getString("language", "pt") ?: "pt"
        tvLabel?.text = langLabel(savedLang)
        llSelector?.setOnClickListener { showLanguagePopup(it, tvLabel) }
    }

    protected fun langLabel(lang: String) = when (lang) {
        "en" -> getString(R.string.lang_en)
        "es" -> getString(R.string.lang_es)
        else -> getString(R.string.lang_pt_br)
    }

    private fun showLanguagePopup(anchor: View, label: TextView?) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 0, 0, getString(R.string.language_portuguese))
        popup.menu.add(0, 1, 1, getString(R.string.language_english))
        popup.menu.add(0, 2, 2, getString(R.string.language_spanish))
        popup.setOnMenuItemClickListener { item ->
            val lang = when (item.itemId) { 1 -> "en"; 2 -> "es"; else -> "pt" }
            prefs.edit().putString("language", lang).apply()
            recreate()
            true
        }
        popup.show()
    }
}
