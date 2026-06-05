package com.example.projecthub.uiscreens.admin.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projecthub.integration.fakeAdminTasksState
import com.example.projecthub.integration.setProjectHubTestContent
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.t
import com.example.projecthub.viewmodel.admin.AdminTaskStatusFilter
import com.example.projecthub.viewmodel.admin.AdminTasksState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminTasksIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val language = AppLanguage.Portuguese

    @Test
    fun taskFilters_emitSearchAndStatusChanges() {
        var search = ""
        var selectedStatus = AdminTaskStatusFilter.All

        composeRule.setProjectHubTestContent {
            TaskFilters(
                state = AdminTasksState(isLoading = false),
                onSearchChange = { search = it },
                onStatusChange = { selectedStatus = it }
            )
        }

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("mobile")
        composeRule.runOnIdle {
            assertEquals("mobile", search)
        }

        composeRule.onNodeWithText(language.t("filters.tasks.all")).performClick()
        composeRule.onNodeWithText(language.t("filters.tasks.completed")).performClick()

        composeRule.runOnIdle {
            assertEquals(AdminTaskStatusFilter.Completed, selectedStatus)
        }
    }

    @Test
    fun projectTaskList_showsExpandedProjectAndTasks() {
        composeRule.setProjectHubTestContent {
            TaskProjectList(
                state = fakeAdminTasksState(expanded = true),
                onToggleProject = {}
            )
        }

        composeRule.onNodeWithText("App Mobile").assertIsDisplayed()
        composeRule.onNodeWithText("Criar CRUD de tarefas").assertIsDisplayed()
        composeRule.onNodeWithText("Validar listagem, estado e datas").assertIsDisplayed()
    }

    @Test
    fun projectTaskList_callsToggleWhenProjectHeaderIsClicked() {
        var toggledProjectId: Int? = null

        composeRule.setProjectHubTestContent {
            TaskProjectList(
                state = fakeAdminTasksState(expanded = false),
                onToggleProject = { toggledProjectId = it }
            )
        }

        composeRule.onNode(hasText("App Mobile") and hasClickAction()).performClick()

        composeRule.runOnIdle {
            assertEquals(2, toggledProjectId)
        }
    }
}
