package com.audiometry.threshold

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class InstrucoesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrucoes)

        setupLanguageSelector(
            findViewById<LinearLayout>(R.id.llLanguageSelector),
            findViewById<TextView>(R.id.tvLanguageLabel)
        )

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            openSideMenu()
        }

        setupExpandable(
            R.id.llChecklistHeader, R.id.llChecklistContent, R.id.ivChecklistArrow
        )
        setupExpandable(
            R.id.llDoubtsHeader, R.id.llDoubtsContent, R.id.ivDoubtsArrow
        )
        setupExpandable(
            R.id.llRespHeader, R.id.llRespContent, R.id.ivRespArrow
        )
    }

    private fun setupExpandable(headerId: Int, contentId: Int, arrowId: Int) {
        val header = findViewById<LinearLayout>(headerId)
        val content = findViewById<LinearLayout>(contentId)
        val arrow = findViewById<ImageView>(arrowId)

        header.setOnClickListener {
            if (content.visibility == View.GONE) {
                content.visibility = View.VISIBLE
                arrow.rotation = 180f
            } else {
                content.visibility = View.GONE
                arrow.rotation = 0f
            }
        }
    }
}
