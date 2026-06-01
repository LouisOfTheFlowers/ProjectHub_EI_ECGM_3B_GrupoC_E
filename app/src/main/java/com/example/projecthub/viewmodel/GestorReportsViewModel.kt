package com.example.projecthub.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.remote.supabase.models.ProjetoUserDto
import com.example.projecthub.remote.supabase.models.RegistoTarefaDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.remote.supabase.models.TarefaUserDto
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
import com.example.projecthub.repository.RegistoTarefaRepository
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class GestorReportExportType(
    val title: String,
    val filePrefix: String
) {
    Users("Utilizadores", "gestor_utilizadores"),
    Projects("Projetos", "gestor_projetos"),
    Tasks("Tarefas", "gestor_tarefas")
}

data class GestorReportSummary(
    val totalUsers: Int = 0,
    val totalProjects: Int = 0,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val delayedTasks: Int = 0,
    val averageCompletion: Int = 0,
    val totalHours: Float = 0f
)

data class GestorReportCard(
    val type: GestorReportExportType,
    val title: String,
    val description: String,
    val rows: Int,
    val primaryMetric: String,
    val secondaryMetric: String
)

data class GestorReportExport(
    val fileName: String,
    val content: String,
    val label: String
)

data class GestorReportsState(
    val summary: GestorReportSummary = GestorReportSummary(),
    val cards: List<GestorReportCard> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val exportErrorMessage: String? = null
)

class GestorReportsViewModel(
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource(),
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository(),
    private val registoTarefaRepository: RegistoTarefaRepository = RegistoTarefaRepository()
) : ViewModel() {

    private data class ReportsSource(
        val users: List<UserDto>,
        val projects: List<ProjetoDto>,
        val tasks: List<TarefaDto>,
        val projectUsers: List<ProjetoUserDto>,
        val taskUsers: List<TarefaUserDto>,
        val records: List<RegistoTarefaDto>
    )

    private var source: ReportsSource? = null

    private val _state = MutableStateFlow(GestorReportsState())
    val stateFlow: StateFlow<GestorReportsState> = _state
    private var state: GestorReportsState
        get() = _state.value
        set(value) { _state.value = value }

    fun loadReports(gestorId: Int?) {
        if (gestorId == null) {
            source = null
            state = GestorReportsState(
                isLoading = false,
                errorMessage = "Não foi possível identificar o gestor autenticado."
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null, exportErrorMessage = null)

            val projectsResult = projetoRepository.getProjetosByGestor(gestorId)
            val tasksResult = tarefaRepository.getTarefas()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }
            val taskUsersResult = tarefaUserRepository.getTarefaUsers()
            val recordsResult = registoTarefaRepository.getRegistos()

            if (
                projectsResult.isFailure ||
                tasksResult.isFailure ||
                usersResult.isFailure ||
                taskUsersResult.isFailure ||
                recordsResult.isFailure
            ) {
                source = null
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os relatórios do gestor."
                )
                return@launch
            }

            val projects = projectsResult.getOrDefault(emptyList())
            val projectIds = projects.mapNotNull { it.id }.toSet()
            val tasks = tasksResult.getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }
            val taskIds = tasks.mapNotNull { it.id }.toSet()
            val taskUsers = taskUsersResult.getOrDefault(emptyList())
                .filter { it.tarefa_id in taskIds }
            val records = recordsResult.getOrDefault(emptyList())
                .filter { it.tarefa_id in taskIds }

            val projectUsers = loadProjectUsers(projectIds)
            val teamUserIds = (projectUsers.map { it.user_id } + taskUsers.map { it.user_id } + records.map { it.user_id })
                .toSet()
            val users = usersResult.getOrDefault(emptyList())
                .filter { it.id in teamUserIds }

            val loadedSource = ReportsSource(
                users = users,
                projects = projects,
                tasks = tasks,
                projectUsers = projectUsers,
                taskUsers = taskUsers,
                records = records
            )
            source = loadedSource

            val completedTasks = tasks.count { it.status.isCompletedStatus() }
            val delayedTasks = tasks.count { it.isDelayed() }
            val totalHours = records.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat()
            val averageCompletion = records.map { it.taxa_conclusao }.averageOrZero().roundToInt()

            state = GestorReportsState(
                summary = GestorReportSummary(
                    totalUsers = users.size,
                    totalProjects = projects.size,
                    totalTasks = tasks.size,
                    completedTasks = completedTasks,
                    delayedTasks = delayedTasks,
                    averageCompletion = averageCompletion,
                    totalHours = totalHours
                ),
                cards = buildCards(
                    source = loadedSource,
                    completedTasks = completedTasks,
                    delayedTasks = delayedTasks,
                    totalHours = totalHours
                ),
                isLoading = false
            )
        }
    }

    fun buildExport(type: GestorReportExportType): GestorReportExport? {
        val currentSource = source

        if (currentSource == null) {
            state = state.copy(exportErrorMessage = "Os dados ainda não estão disponíveis.")
            return null
        }

        val content = when (type) {
            GestorReportExportType.Users -> buildUsersCsv(currentSource)
            GestorReportExportType.Projects -> buildProjectsCsv(currentSource)
            GestorReportExportType.Tasks -> buildTasksCsv(currentSource)
        }

        state = state.copy(exportErrorMessage = null)

        return GestorReportExport(
            fileName = "projecthub_${type.filePrefix}_${LocalDate.now()}.csv",
            content = content,
            label = type.title
        )
    }

    fun setExportError(message: String) {
        state = state.copy(exportErrorMessage = message)
    }

    private suspend fun loadProjectUsers(projectIds: Set<Int>): List<ProjetoUserDto> {
        return projectIds.flatMap { projectId ->
            projetoUserRepository.getUsersByProjeto(projectId).getOrDefault(emptyList())
        }.distinctBy { "${it.projeto_id}-${it.user_id}" }
    }

    private fun buildCards(
        source: ReportsSource,
        completedTasks: Int,
        delayedTasks: Int,
        totalHours: Float
    ): List<GestorReportCard> {
        val usersWithTasks = source.taskUsers.map { it.user_id }.distinct().size
        val completedProjects = source.projects.count { it.status.isCompletedStatus() }

        return listOf(
            GestorReportCard(
                type = GestorReportExportType.Users,
                title = "Estatísticas por utilizador",
                description = "Equipa associada aos teus projetos, tarefas atribuídas, progresso e tempo registado.",
                rows = source.users.size,
                primaryMetric = "$usersWithTasks com tarefas",
                secondaryMetric = "${source.projectUsers.size} associações"
            ),
            GestorReportCard(
                type = GestorReportExportType.Projects,
                title = "Estatísticas por projeto",
                description = "Projetos atribuídos a ti, equipa, tarefas concluídas, atrasos, progresso e horas.",
                rows = source.projects.size,
                primaryMetric = "$completedProjects concluídos",
                secondaryMetric = "$delayedTasks tarefas atrasadas"
            ),
            GestorReportCard(
                type = GestorReportExportType.Tasks,
                title = "Estatísticas por tarefa",
                description = "Tarefas dos teus projetos com responsáveis, registos, conclusão média e tempo gasto.",
                rows = source.tasks.size,
                primaryMetric = "$completedTasks completadas",
                secondaryMetric = "${totalHours.formatNumber()} h"
            )
        )
    }

    private fun buildUsersCsv(source: ReportsSource): String {
        val projectsById = source.projects.mapNotNull { project -> project.id?.let { it to project } }.toMap()
        val tasksById = source.tasks.mapNotNull { task -> task.id?.let { it to task } }.toMap()
        val projectIdsByUser = source.projectUsers
            .groupBy { it.user_id }
            .mapValues { (_, links) -> links.map { it.projeto_id }.toSet() }
        val taskIdsByUser = source.taskUsers
            .groupBy { it.user_id }
            .mapValues { (_, links) -> links.map { it.tarefa_id }.toSet() }
        val recordsByUser = source.records.groupBy { it.user_id }

        val rows = source.users
            .sortedBy { it.nome.lowercase() }
            .map { user ->
                val userId = user.id ?: 0
                val userProjectIds = projectIdsByUser[userId].orEmpty()
                val assignedTaskIds = taskIdsByUser[userId].orEmpty()
                val records = recordsByUser[userId].orEmpty()
                val completedAssignedTasks = assignedTaskIds.count { taskId ->
                    tasksById[taskId]?.status?.isCompletedStatus() == true
                }
                val projectNames = userProjectIds
                    .mapNotNull { projectsById[it]?.nome }
                    .sorted()
                    .joinToString(", ")

                listOf(
                    userId.toString(),
                    user.nome,
                    user.username,
                    user.email,
                    user.status,
                    projectNames,
                    userProjectIds.size.toString(),
                    assignedTaskIds.size.toString(),
                    completedAssignedTasks.toString(),
                    records.size.toString(),
                    records.map { it.taxa_conclusao }.averageOrZero().roundToInt().toString(),
                    records.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat().formatNumber()
                )
            }

        return buildCsv(
            headers = listOf(
                "user_id",
                "nome",
                "username",
                "email",
                "status",
                "projetos",
                "total_projetos",
                "tarefas_atribuidas",
                "tarefas_concluidas",
                "registos",
                "taxa_media_conclusao_percentagem",
                "tempo_total_horas"
            ),
            rows = rows
        )
    }

    private fun buildProjectsCsv(source: ReportsSource): String {
        val tasksByProject = source.tasks.groupBy { it.projeto_id }
        val memberCountsByProject = source.projectUsers.groupingBy { it.projeto_id }.eachCount()
        val recordsByTask = source.records.groupBy { it.tarefa_id }

        val rows = source.projects
            .sortedBy { it.nome.lowercase() }
            .map { project ->
                val projectId = project.id ?: 0
                val projectTasks = tasksByProject[projectId].orEmpty()
                val taskIds = projectTasks.mapNotNull { it.id }.toSet()
                val projectRecords = taskIds.flatMap { taskId -> recordsByTask[taskId].orEmpty() }
                val completedTasks = projectTasks.count { it.status.isCompletedStatus() }
                val pendingTasks = projectTasks.size - completedTasks
                val delayedTasks = projectTasks.count { it.isDelayed() }
                val progress = if (projectTasks.isEmpty()) {
                    projectRecords.map { it.taxa_conclusao }.averageOrZero().roundToInt()
                } else {
                    ((completedTasks.toFloat() / projectTasks.size.toFloat()) * 100f).roundToInt()
                }

                listOf(
                    projectId.toString(),
                    project.nome,
                    project.status,
                    project.data_inicio.toCsvDate(),
                    project.data_fim.toCsvDate(),
                    (memberCountsByProject[projectId] ?: 0).toString(),
                    projectTasks.size.toString(),
                    completedTasks.toString(),
                    pendingTasks.toString(),
                    delayedTasks.toString(),
                    progress.toString(),
                    projectRecords.map { it.taxa_conclusao }.averageOrZero().roundToInt().toString(),
                    projectRecords.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat().formatNumber(),
                    projectRecords.size.toString()
                )
            }

        return buildCsv(
            headers = listOf(
                "projeto_id",
                "projeto",
                "status",
                "data_inicio",
                "data_fim",
                "membros",
                "tarefas",
                "tarefas_concluidas",
                "tarefas_pendentes",
                "tarefas_atrasadas",
                "progresso_percentagem",
                "taxa_media_conclusao_percentagem",
                "tempo_total_horas",
                "registos"
            ),
            rows = rows
        )
    }

    private fun buildTasksCsv(source: ReportsSource): String {
        val projectsById = source.projects.mapNotNull { project -> project.id?.let { it to project.nome } }.toMap()
        val userCountsByTask = source.taskUsers.groupingBy { it.tarefa_id }.eachCount()
        val recordsByTask = source.records.groupBy { it.tarefa_id }

        val rows = source.tasks
            .sortedWith(compareBy<TarefaDto> { projectsById[it.projeto_id].orEmpty().lowercase() }.thenBy { it.titulo.lowercase() })
            .map { task ->
                val taskId = task.id ?: 0
                val records = recordsByTask[taskId].orEmpty()

                listOf(
                    taskId.toString(),
                    task.titulo,
                    task.descricao.orEmpty(),
                    projectsById[task.projeto_id].orEmpty(),
                    task.status,
                    task.data_inicio.toCsvDate(),
                    task.data_fim.toCsvDate(),
                    (userCountsByTask[taskId] ?: 0).toString(),
                    records.size.toString(),
                    records.map { it.taxa_conclusao }.averageOrZero().roundToInt().toString(),
                    records.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat().formatNumber(),
                    if (task.isDelayed()) "Sim" else "Não"
                )
            }

        return buildCsv(
            headers = listOf(
                "tarefa_id",
                "tarefa",
                "descricao",
                "projeto",
                "status",
                "data_inicio",
                "data_fim",
                "utilizadores_atribuidos",
                "registos",
                "taxa_media_conclusao_percentagem",
                "tempo_total_horas",
                "atrasada"
            ),
            rows = rows
        )
    }

    private fun buildCsv(
        headers: List<String>,
        rows: List<List<String>>
    ): String {
        return buildString {
            appendLine(headers.joinToString(";") { it.toCsvCell() })
            rows.forEach { row ->
                appendLine(row.joinToString(";") { it.toCsvCell() })
            }
        }
    }

    private fun String.toCsvCell(): String {
        val escaped = replace("\"", "\"\"")
            .replace("\r", " ")
            .replace("\n", " ")

        return "\"$escaped\""
    }

    private fun TarefaDto.isDelayed(): Boolean {
        val dueDate = data_fim.toLocalDateOrNull() ?: return false
        return !status.isCompletedStatus() && dueDate.isBefore(LocalDate.now())
    }

    private fun ProjetoDto.isDelayed(): Boolean {
        val dueDate = data_fim.toLocalDateOrNull() ?: return false
        return !status.isCompletedStatus() && dueDate.isBefore(LocalDate.now())
    }

    private fun String?.toLocalDateOrNull(): LocalDate? {
        return this?.let { value ->
            runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
        }
    }

    private fun String?.toCsvDate(): String {
        val date = toLocalDateOrNull() ?: return orEmpty()
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun String.isCompletedStatus(): Boolean {
        return normalizedStatus() in setOf(
            "CONCLUIDO",
            "CONCLUIDA",
            "COMPLETO",
            "COMPLETA",
            "COMPLETADO",
            "COMPLETADA",
            "FINALIZADO",
            "FINALIZADA"
        )
    }

    private fun String.normalizedStatus(): String {
        val withoutAccents = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return withoutAccents
            .trim()
            .replace(" ", "_")
            .replace("-", "_")
            .uppercase()
    }

    private fun Iterable<Int>.averageOrZero(): Double {
        val values = toList()
        return if (values.isEmpty()) 0.0 else values.average()
    }

    private fun Float.formatNumber(): String {
        return String.format(Locale.US, "%.1f", this)
    }
}
