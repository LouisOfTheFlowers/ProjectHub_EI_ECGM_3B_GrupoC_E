package com.example.projecthub.integration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.example.projecthub.remote.supabase.models.ObservacaoDto
import com.example.projecthub.remote.supabase.models.ObservacaoFotoDto
import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.remote.supabase.models.RegistoTarefaDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.ui.theme.ProjectHubTheme
import com.example.projecthub.viewmodel.admin.AdminProjectTaskGroup
import com.example.projecthub.viewmodel.admin.AdminTaskListItem
import com.example.projecthub.viewmodel.admin.AdminTaskProjectOption
import com.example.projecthub.viewmodel.admin.AdminTasksState
import com.example.projecthub.viewmodel.admin.AdminTeamProjectOption
import com.example.projecthub.viewmodel.admin.AdminTeamUserItem
import com.example.projecthub.viewmodel.admin.AdminTeamsState
import com.example.projecthub.viewmodel.utilizador.UtilizadorProjectItem
import com.example.projecthub.viewmodel.utilizador.UtilizadorTaskItem
import com.example.projecthub.viewmodel.utilizador.UtilizadorTaskObservation

fun ComposeContentTestRule.setProjectHubTestContent(content: @Composable () -> Unit) {
    setContent {
        ProjectHubTheme(darkTheme = false, dynamicColor = false) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

fun fakeTask(
    id: Int = 10,
    title: String = "Implementar painel",
    status: String = "PENDENTE",
    projectId: Int = 2
): TarefaDto {
    return TarefaDto(
        id = id,
        titulo = title,
        descricao = "Criar interface e validar estados principais",
        projeto_id = projectId,
        status = status,
        data_inicio = "2026-05-20",
        data_fim = "2026-05-27"
    )
}

fun fakeUserTaskItem(
    task: TarefaDto = fakeTask(),
    observations: List<UtilizadorTaskObservation> = listOf(fakeTaskObservation(taskId = task.id ?: 10))
): UtilizadorTaskItem {
    return UtilizadorTaskItem(
        task = task,
        projectName = "App Mobile",
        recordsCount = observations.size,
        observations = observations
    )
}

fun fakeTaskObservation(taskId: Int = 10): UtilizadorTaskObservation {
    return UtilizadorTaskObservation(
        observation = ObservacaoDto(
            id = 31,
            registo_id = 21,
            texto = "Validei o fluxo principal no dispositivo."
        ),
        record = RegistoTarefaDto(
            id = 21,
            tarefa_id = taskId,
            user_id = 7,
            data = "2026-05-26",
            local = "Porto",
            taxa_conclusao = 70,
            tempo_gasto = 2.5f
        ),
        photos = listOf(
            ObservacaoFotoDto(
                id = 41,
                observacao_id = 31,
                foto_url = "https://example.com/foto-teste.jpg"
            )
        )
    )
}

fun fakeProjectItem(
    projectId: Int = 4,
    completedTaskHistory: List<TarefaDto> = listOf(
        fakeTask(id = 77, title = "Finalizar validacoes", status = "CONCLUIDO", projectId = projectId)
    )
): UtilizadorProjectItem {
    return UtilizadorProjectItem(
        project = ProjetoDto(
            id = projectId,
            nome = "Project Hub Mobile",
            descricao = "Gestao de projetos academicos",
            data_inicio = "2026-05-01",
            data_fim = "2026-06-15",
            status = "EM PROGRESSO",
            gestor_id = 3
        ),
        tasksCount = 4,
        completedTasks = completedTaskHistory.size,
        lateTasks = 1,
        completedTaskHistory = completedTaskHistory
    )
}

fun fakeAdminTasksState(expanded: Boolean = true): AdminTasksState {
    val task = AdminTaskListItem(
        id = 88,
        title = "Criar CRUD de tarefas",
        description = "Validar listagem, estado e datas",
        statusLabel = "Pendente",
        startDate = "2026-05-20",
        dueDate = "2026-05-30",
        isCompleted = false,
        isDelayed = false
    )

    return AdminTasksState(
        projectGroups = listOf(
            AdminProjectTaskGroup(
                projectId = 2,
                projectName = "App Mobile",
                totalTasks = 1,
                visibleTasks = listOf(task),
                completedTasks = 0,
                pendingTasks = 1,
                isExpanded = expanded
            )
        ),
        projects = listOf(AdminTaskProjectOption(id = 2, name = "App Mobile")),
        totalTasks = 1,
        pendingTasks = 1,
        completedTasks = 0,
        isLoading = false
    )
}

fun fakeAdminTeamUser(): AdminTeamUserItem {
    val source = UserDto(
        id = 5,
        nome = "Filipa Costa",
        username = "filipa",
        email = "filipa@example.com",
        role = "UTILIZADOR",
        status = "ATIVO"
    )

    return AdminTeamUserItem(
        id = 5,
        name = source.nome,
        username = source.username,
        email = source.email,
        role = source.role,
        status = source.status,
        projectNames = listOf("App Mobile"),
        source = source
    )
}

fun fakeAdminTeamsState(): AdminTeamsState {
    val user = fakeAdminTeamUser()

    return AdminTeamsState(
        users = listOf(user),
        visibleUsers = listOf(user),
        projects = listOf(AdminTeamProjectOption(id = 2, name = "App Mobile")),
        roles = listOf("GESTOR", "UTILIZADOR"),
        totalUsers = 1,
        activeUsers = 1,
        isLoading = false
    )
}
