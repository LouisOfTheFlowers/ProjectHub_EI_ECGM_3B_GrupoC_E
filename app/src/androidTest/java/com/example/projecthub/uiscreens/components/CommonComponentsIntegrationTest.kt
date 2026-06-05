package com.example.projecthub.uiscreens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projecthub.integration.setProjectHubTestContent
import com.example.projecthub.settings.AppLanguage
import com.example.projecthub.settings.t
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommonComponentsIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val language = AppLanguage.Portuguese

    @Test
    fun offlineState_showsOfflineMessageAndIconDescription() {
        composeRule.setProjectHubTestContent {
            AppOfflineState()
        }

        composeRule.onNodeWithContentDescription(language.t("offline.title")).assertIsDisplayed()
        composeRule.onNodeWithText(language.t("offline.title")).assertIsDisplayed()
        composeRule.onNodeWithText(language.t("offline.detail")).assertIsDisplayed()
    }

    @Test
    fun messageCardAndStatusChip_renderSharedUiText() {
        composeRule.setProjectHubTestContent {
            Column {
                AppMessageCard(
                    title = "Estado da aplicacao",
                    detail = "Tudo preparado para sincronizar."
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppStatusChip(text = "CONCLUIDO")
            }
        }

        composeRule.onNodeWithText("Estado da aplicacao").assertIsDisplayed()
        composeRule.onNodeWithText("Tudo preparado para sincronizar.").assertIsDisplayed()
        composeRule.onNodeWithText(language.t("common.completed")).assertIsDisplayed()
    }
}
