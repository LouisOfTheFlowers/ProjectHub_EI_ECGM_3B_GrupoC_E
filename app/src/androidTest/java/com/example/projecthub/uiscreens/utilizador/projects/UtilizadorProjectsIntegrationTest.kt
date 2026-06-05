package com.example.projecthub.uiscreens.utilizador.projects

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projecthub.integration.fakeProjectItem
import com.example.projecthub.integration.setProjectHubTestContent
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilizadorProjectsIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val language = AppLanguage.Portuguese

    @Test
    fun projectList_showsProjectMetricsAndOpensHistory() {
        var openedProjectId: Int? = null

        composeRule.setProjectHubTestContent {
            UtilizadorProjectsSection(
                state = UtilizadorProjectsState(projects = listOf(fakeProjectItem())),
                projectHistoryId = null,
                onOpenHistory = { openedProjectId = it },
                onBack = {}
            )
        }

        composeRule.onNodeWithText(language.t("user.projects.title")).assertIsDisplayed()
        composeRule.onNodeWithText("Project Hub Mobile").assertIsDisplayed()
        composeRule.onNodeWithText(language.t("user.projects.viewHistory")).performClick()

        composeRule.runOnIdle {
            assertEquals(4, openedProjectId)
        }
    }

    @Test
    fun historyPage_showsCompletedTasksAndHandlesBack() {
        var wentBack = false

        composeRule.setProjectHubTestContent {
            UtilizadorProjectsSection(
                state = UtilizadorProjectsState(projects = listOf(fakeProjectItem())),
                projectHistoryId = 4,
                onOpenHistory = {},
                onBack = { wentBack = true }
            )
        }

        composeRule.onNodeWithText(language.t("user.projects.historySubtitle")).assertIsDisplayed()
        composeRule.onNodeWithText("Finalizar validacoes").assertIsDisplayed()

        composeRule.onNodeWithText(language.t("user.projects.back")).performClick()

        composeRule.runOnIdle {
            assertTrue(wentBack)
        }
    }
}
