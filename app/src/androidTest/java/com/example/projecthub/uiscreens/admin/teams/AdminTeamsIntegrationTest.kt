package com.example.projecthub.uiscreens.admin.teams

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projecthub.integration.fakeAdminTeamUser
import com.example.projecthub.integration.fakeAdminTeamsState
import com.example.projecthub.integration.setProjectHubTestContent
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.t
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminTeamsIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val language = AppLanguage.Portuguese

    @Test
    fun teamFilters_emitSearchRoleAndProjectSelections() {
        var search = ""
        var role: String? = null
        var projectId: Int? = null

        composeRule.setProjectHubTestContent {
            TeamFilters(
                state = fakeAdminTeamsState(),
                onSearchChange = { search = it },
                onRoleSelected = { role = it },
                onProjectSelected = { projectId = it }
            )
        }

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("filipa")
        composeRule.runOnIdle {
            assertEquals("filipa", search)
        }

        composeRule.onNode(hasText(language.t("common.allFemale")) and hasClickAction()).performClick()
        composeRule.onNodeWithText(language.t("role.manager")).performClick()

        composeRule.onNode(hasText(language.t("common.all")) and hasClickAction()).performClick()
        composeRule.onNodeWithText("App Mobile").performClick()

        composeRule.runOnIdle {
            assertEquals("GESTOR", role)
            assertEquals(2, projectId)
        }
    }

    @Test
    fun teamUserList_showsUserAndCallsDelete() {
        var deletedUserId: Int? = null

        composeRule.setProjectHubTestContent {
            TeamUserList(
                state = fakeAdminTeamsState(),
                onRoleSelected = { _, _ -> },
                onDeleteUser = { deletedUserId = it.id }
            )
        }

        composeRule.onNodeWithText("Filipa Costa").assertIsDisplayed()
        composeRule.onNodeWithText("@filipa").assertIsDisplayed()
        composeRule.onNodeWithText("filipa@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("App Mobile").assertIsDisplayed()

        composeRule.onNodeWithText(language.t("teams.remove")).performClick()

        composeRule.runOnIdle {
            assertEquals(5, deletedUserId)
        }
    }

    @Test
    fun userRoleEditor_emitsSelectedRole() {
        val user = fakeAdminTeamUser()
        var selectedRole: String? = null

        composeRule.setProjectHubTestContent {
            UserRoleEditor(
                user = user,
                isUpdating = false,
                isDeleting = false,
                onRoleSelected = { _, role -> selectedRole = role },
                onDelete = {}
            )
        }

        composeRule.onNode(hasText(language.t("role.user")) and hasClickAction()).performClick()
        composeRule.onNodeWithText(language.t("role.manager")).performClick()

        composeRule.runOnIdle {
            assertEquals("GESTOR", selectedRole)
        }
    }
}
