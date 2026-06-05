package com.example.projecthub.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppStringsTest {

    @Test
    fun t_returnsPortugueseTranslationForKnownKey() {
        assertEquals("Conclu\u00eddas", AppLanguage.Portuguese.t("common.completed"))
        assertEquals("Gestor", AppLanguage.Portuguese.t("role.manager"))
    }

    @Test
    fun t_returnsEnglishTranslationForKnownKey() {
        assertEquals("Completed", AppLanguage.English.t("common.completed"))
        assertEquals("Manager", AppLanguage.English.t("role.manager"))
    }

    @Test
    fun t_returnsSpanishTranslationForKnownKey() {
        assertEquals("Completadas", AppLanguage.Spanish.t("common.completed"))
        assertEquals("Usuario", AppLanguage.Spanish.t("role.user"))
    }

    @Test
    fun t_returnsKeyWhenTranslationDoesNotExist() {
        assertEquals("missing.key", AppLanguage.Portuguese.t("missing.key"))
        assertEquals("missing.key", AppLanguage.English.t("missing.key"))
        assertEquals("missing.key", AppLanguage.Spanish.t("missing.key"))
    }
}
