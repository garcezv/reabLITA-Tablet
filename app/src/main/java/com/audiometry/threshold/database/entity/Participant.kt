package com.audiometry.threshold.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participants")
data class Participant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cpf: String = "",
    val nomeCompleto: String = "",
    val dataNascimento: String = "",
    val nomeSocial: String = "",
    val rg: String = "",
    val orgaoExpedidor: String = "",
    val dataExpedicao: String = "",
    val sexoBiologico: String = "",
    val identidadeGenero: String = "",
    val racaCor: String = "",
    val nomeMae: String = "",
    val nomePai: String = "",
    val emailPessoal: String = "",
    val telefone: String = "",
    val cep: String = "",
    val estado: String = "",
    val municipio: String = "",
    val bairro: String = "",
    val logradouro: String = "",
    val numero: String = "",
    val complemento: String = "",
    val instituicao: String = "",
    val professorResponsavel: String = "",
    val queixaAuditiva: String = "Não",
    val detalhesQueixa: String = "",
    val outrasObservacoes: String = "",
    val statusAuditivo: String = "Normal",
    val testeRealizado: String = "Aud.IT",
    val dataUltimoTeste: String = "",
    val necessitaReavaliacao: String = "Não"
)
