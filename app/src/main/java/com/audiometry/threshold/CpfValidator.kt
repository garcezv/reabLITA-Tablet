package com.audiometry.threshold

object CpfValidator {

    fun isValid(cpf: String): Boolean {
        val d = cpf.replace(Regex("[^0-9]"), "")
        if (d.length != 11) return false
        if (d.all { it == d[0] }) return false
        return d[9].digitToInt() == checkDigit(d, 9) &&
               d[10].digitToInt() == checkDigit(d, 10)
    }

    private fun checkDigit(d: String, pos: Int): Int {
        val factor = pos + 1
        val sum = (0 until pos).sumOf { d[it].digitToInt() * (factor - it) }
        val rem = sum % 11
        return if (rem < 2) 0 else 11 - rem
    }

    fun format(cpf: String): String {
        val d = cpf.replace(Regex("[^0-9]"), "").take(11)
        return buildString {
            d.forEachIndexed { i, c ->
                append(c)
                when (i) {
                    2, 5 -> if (i < d.lastIndex) append('.')
                    8    -> if (i < d.lastIndex) append('-')
                }
            }
        }
    }
}
