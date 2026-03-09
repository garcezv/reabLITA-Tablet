package com.audiometry.threshold

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val cardOuvir = findViewById<CardView>(R.id.cardOuvir)
        val cardAudit = findViewById<CardView>(R.id.cardAudit)
        cardOuvir.doOnLayout {
            val maxH = maxOf(cardOuvir.height, cardAudit.height)
            if (cardOuvir.height != maxH) cardOuvir.updateLayoutParams { height = maxH }
            if (cardAudit.height != maxH) cardAudit.updateLayoutParams { height = maxH }
        }

        setupLanguageSelector(
            findViewById<LinearLayout>(R.id.llLanguageSelector),
            findViewById<TextView>(R.id.tvLanguageLabel)
        )

        findViewById<Button>(R.id.btnLearnMore).setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            openSideMenu()
        }

        findViewById<Button>(R.id.btnPanelOuvir).setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStartOuvir).setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_feature_unavailable), Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnPanelAudit).setOnClickListener {
            startActivity(Intent(this, PainelAuditActivity::class.java))
        }

        findViewById<Button>(R.id.btnStartAudit).setOnClickListener {
            SelectParticipantsDialog().show(supportFragmentManager, "selectParticipants")
        }
    }

    override fun onBackPressed() {
        // Prevent accidental back press — show confirmation or do nothing
        super.onBackPressed()
    }
}
