package com.audiometry.threshold

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class ViaCepAddress(
    val cep: String,
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String
)

object ViaCepService {
    suspend fun fetch(cep: String): ViaCepAddress? = withContext(Dispatchers.IO) {
        try {
            val digits = cep.replace(Regex("[^0-9]"), "")
            if (digits.length != 8) return@withContext null
            val json = URL("https://viacep.com.br/ws/$digits/json/").readText()
            val obj = JSONObject(json)
            if (obj.optBoolean("erro", false)) return@withContext null
            ViaCepAddress(
                cep        = obj.optString("cep", ""),
                logradouro = obj.optString("logradouro", ""),
                bairro     = obj.optString("bairro", ""),
                localidade = obj.optString("localidade", ""),
                uf         = obj.optString("uf", "")
            )
        } catch (_: Exception) {
            null
        }
    }
}
