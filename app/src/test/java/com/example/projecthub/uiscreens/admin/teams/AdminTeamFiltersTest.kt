package com.example.projecthub.uiscreens.admin.teams

import com.example.projecthub.settings.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminTeamFiltersTest {

    @Test
    fun toRoleFilterLabel_translatesKnownRolesInPortuguese() {
        assertEquals("Admin", "ADMIN".toRoleFilterLabel(AppLanguage.Portuguese))
        assertEquals("Gestor", "GESTOR".toRoleFilterLabel(AppLanguage.Portuguese))
        assertEquals("Utilizador", "UTILIZADOR".toRoleFilterLabel(AppLanguage.Portuguese))
    }

    @Test
    fun toRoleFilterLabel_translatesKnownRolesInEnglish() {
        assertEquals("Admin", "ADMIN".toRoleFilterLabel(AppLanguage.English))
        assertEquals("Manager", "GESTOR".toRoleFilterLabel(AppLanguage.English))
        assertEquals("User", "UTILIZADOR".toRoleFilterLabel(AppLanguage.English))
    }

    @Test
    fun toRoleFilterLabel_keepsUnknownRole() {
        assertEquals("SUPERVISOR", "SUPERVISOR".toRoleFilterLabel(AppLanguage.Portuguese))
    }
}
