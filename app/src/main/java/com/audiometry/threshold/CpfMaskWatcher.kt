package com.audiometry.threshold

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CpfMaskWatcher(
    private val editText: TextInputEditText,
    private val tilCpf: TextInputLayout,
    private val context: Context
) : TextWatcher {

    private var lock = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (lock || s == null) return
        lock = true

        val digits = s.toString().replace(Regex("[^0-9]"), "").take(11)
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                append(c)
                when (i) {
                    2, 5 -> if (i < digits.lastIndex) append('.')
                    8    -> if (i < digits.lastIndex) append('-')
                }
            }
        }
        s.replace(0, s.length, formatted)

        tilCpf.error = when {
            digits.length == 11 && !CpfValidator.isValid(digits) ->
                context.getString(R.string.cpf_invalid)
            else -> null
        }

        lock = false
    }
}
