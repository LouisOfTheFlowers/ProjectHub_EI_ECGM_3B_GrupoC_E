package com.example.projecthub.models

import com.example.projecthub.local.entities.SyncQueueEntity
import com.example.projecthub.local.entities.UserEntity
import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.remote.supabase.models.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ModelsDefaultsTest {

    @Test
    fun userDto_usesExpectedDefaults() {
        val user = UserDto(
            nome = "Ana",
            username = "ana",
            email = "ana@email.com"
        )

        assertNull(user.id)
        assertEquals("", user.password)
        assertNull(user.foto)
        assertEquals("UTILIZADOR", user.role)
        assertEquals("PENDENTE", user.status)
    }

    @Test
    fun projetoDto_usesExpectedDefaults() {
        val project = ProjetoDto(nome = "Projeto")

        assertNull(project.id)
        assertNull(project.descricao)
        assertNull(project.data_inicio)
        assertNull(project.data_fim)
        assertEquals("PENDENTE", project.status)
        assertNull(project.gestor_id)
    }

    @Test
    fun tarefaDto_usesExpectedDefaults() {
        val task = TarefaDto(
            titulo = "Tarefa",
            projeto_id = 1
        )

        assertNull(task.id)
        assertNull(task.descricao)
        assertEquals("PENDENTE", task.status)
        assertNull(task.data_inicio)
        assertNull(task.data_fim)
    }

    @Test
    fun syncQueueEntity_startsAsPendingSyncAction() {
        val action = SyncQueueEntity(
            action = "UPDATE_PROFILE",
            payload = "{}"
        )

        assertEquals(0, action.id)
        assertEquals("UPDATE_PROFILE", action.action)
        assertEquals("{}", action.payload)
        assertFalse(action.synced)
    }

    @Test
    fun userEntity_keepsLocalProfileFields() {
        val user = UserEntity(
            nome = "Bruno",
            username = "bruno",
            email = "bruno@email.com",
            password = "12345678",
            role = "GESTOR",
            status = "ATIVO"
        )

        assertEquals(0, user.id)
        assertEquals("Bruno", user.nome)
        assertEquals("bruno", user.username)
        assertEquals("bruno@email.com", user.email)
        assertEquals("GESTOR", user.role)
        assertEquals("ATIVO", user.status)
        assertNull(user.foto)
    }
}
