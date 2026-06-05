package com.example.projecthub.uiscreens.utilizador.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projecthub.integration.fakeTask
import com.example.projecthub.integration.fakeUserTaskItem
import com.example.projecthub.integration.setProjectHubTestContent
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.utilizador.UtilizadorTasksState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilizadorTasksIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val language = AppLanguage.Portuguese

    @Test
    fun taskList_showsTaskDataAndOpensObservations() {
        var openedTaskId: Int? = null

        composeRule.setProjectHubTestContent {
            UtilizadorTasksSection(
                state = UtilizadorTasksState(tasks = listOf(fakeUserTaskItem())),
                taskObservationsId = null,
                onOpenObservations = { openedTaskId = it },
                onBack = {},
                onAddObservation = { _, _, _ -> },
                onCompleteTask = { _, _, _, _ -> }
            )
        }

        composeRule.onNodeWithText(language.t("user.tasks.title")).assertIsDisplayed()
        composeRule.onNodeWithText("Implementar painel").assertIsDisplayed()
        composeRule.onNodeWithText("App Mobile").assertIsDisplayed()

        composeRule.onNodeWithText(language.t("user.tasks.observations")).performClick()

        composeRule.runOnIdle {
            assertEquals(10, openedTaskId)
        }
    }

    @Test
    fun observationsPage_showsExistingObservationAndSavesNewObservation() {
        var savedText: String? = null

        composeRule.setProjectHubTestContent {
            UtilizadorTasksSection(
                state = UtilizadorTasksState(tasks = listOf(fakeUserTaskItem())),
                taskObservationsId = 10,
                onOpenObservations = {},
                onBack = {},
                onAddObservation = { _, text, _ -> savedText = text },
                onCompleteTask = { _, _, _, _ -> }
            )
        }

        composeRule.onNodeWithText(language.t("user.tasks.observationsTitle")).assertIsDisplayed()
        composeRule.onNodeWithText("Validei o fluxo principal no dispositivo.").assertIsDisplayed()

        composeRule.onNodeWithText(language.t("user.tasks.addObservation")).performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Nova observacao de teste")
        composeRule.onNodeWithText(language.t("common.save")).performClick()

        composeRule.runOnIdle {
            assertEquals("Nova observacao de teste", savedText)
        }
    }

    @Test
    fun taskList_opensCompleteDialogForPendingTask() {
        composeRule.setProjectHubTestContent {
            UtilizadorTasksSection(
                state = UtilizadorTasksState(
                    tasks = listOf(fakeUserTaskItem(task = fakeTask(status = "PENDENTE")))
                ),
                taskObservationsId = null,
                onOpenObservations = {},
                onBack = {},
                onAddObservation = { _, _, _ -> },
                onCompleteTask = { _, _, _, _ -> }
            )
        }

        composeRule.onAllNodesWithText(language.t("user.tasks.complete"))[0].performClick()

        composeRule.onNodeWithText(language.t("user.tasks.completeTitle")).assertIsDisplayed()
        composeRule.onNodeWithText(language.t("user.tasks.completionLocation")).assertIsDisplayed()
        composeRule.onNodeWithText(language.t("user.tasks.spentHours")).assertIsDisplayed()
    }
}
