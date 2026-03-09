package com.audiometry.threshold.database

import com.audiometry.threshold.database.entity.Participant
import com.audiometry.threshold.database.entity.User

object SampleDataHelper {

    private val sampleNames = listOf(
        "Pedro Eduardo dos Santos",
        "Maria Luisa Ferreira",
        "João Carlos Oliveira",
        "Ana Carolina Silva",
        "Lucas Henrique Souza",
        "Fernanda Cristina Lima",
        "Gabriel Augusto Pereira",
        "Juliana Beatriz Costa",
        "Rafael Mateus Rodrigues",
        "Isabela Vitória Martins",
        "Thiago Alexandre Alves",
        "Larissa Beatriz Nascimento",
        "Mateus Felipe Carvalho",
        "Camila Roberta Gomes",
        "Bruno Leonardo Barbosa",
        "Amanda Patricia Ribeiro",
        "Diego Fernando Teixeira",
        "Vanessa Cristina Correia",
        "Rodrigo Antonio Mendes",
        "Patricia Helena Cavalcante"
    )

    suspend fun populate(db: AppDatabase) {
        val userDao = db.userDao()
        val participantDao = db.participantDao()

        if (userDao.count() == 0) {
            userDao.insert(User(
                email = "admin@reablita.org",
                password = "123456",
                name = "Administrador",
                role = "Administrador(a)"
            ))
            userDao.insert(User(
                email = "facilitador@reablita.org",
                password = "reablita",
                name = "Fulana de Ta",
                role = "Facilitador(a)"
            ))
        }

        if (participantDao.count() == 0) {
            val testTypes = listOf("Aud.IT", "Ouvir Brasil")
            val statuses = listOf("Normal", "Normal", "Normal", "Alterado")
            val reevals = listOf("Não", "Não", "Não", "Sim")
            val dates = listOf(
                "16/02/2025", "15/02/2025", "14/02/2025",
                "12/02/2025", "10/02/2025", "08/02/2025"
            )
            val mothers = listOf(
                "Maria Santos", "Ana Ferreira", "Lucia Oliveira", "Rosa Silva",
                "Clara Souza", "Helena Lima", "Beatriz Pereira", "Claudia Costa",
                "Vera Rodrigues", "Elaine Martins", "Silvia Alves", "Patricia Nascimento",
                "Marcia Carvalho", "Regina Gomes", "Tereza Barbosa", "Fatima Ribeiro",
                "Denise Teixeira", "Sandra Correia", "Rosana Mendes", "Lucia Cavalcante"
            )
            val schools = listOf(
                "Escola Municipal São João", "EMEF Presidente Vargas",
                "Colégio Estadual Central", "Escola Básica Norte",
                "EMEI Jardim Flores", "Escola Municipal Sul"
            )

            sampleNames.forEachIndexed { index, name ->
                participantDao.insert(
                    Participant(
                        nomeCompleto = name,
                        cpf = "000.000.0${index.toString().padStart(2, '0')}-0${index % 10}",
                        dataNascimento = "0${(index % 9) + 1}/0${(index % 11) + 1}/${2010 + (index % 10)}",
                        nomeMae = mothers[index],
                        testeRealizado = testTypes[index % testTypes.size],
                        dataUltimoTeste = dates[index % dates.size],
                        statusAuditivo = statuses[index % statuses.size],
                        necessitaReavaliacao = reevals[index % reevals.size],
                        instituicao = schools[index % schools.size],
                        queixaAuditiva = if (index % 4 == 0) "Sim" else "Não",
                        estado = "SP"
                    )
                )
            }
        }
    }
}
