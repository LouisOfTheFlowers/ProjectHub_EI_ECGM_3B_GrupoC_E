package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

enum class AdminReportExportType(
    val title: String,
    val filePrefix: String
) {
    Users("Utilizadores", "utilizadores"),
    Projects("Projetos", "projetos"),
    Tasks("Tarefas", "tarefas")
}

data class AdminReportSummary(
    val totalUsers: Int = 0,
    val totalProjects: Int = 0,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val delayedTasks: Int = 0,
    val averageCompletion: Int = 0,
    val totalHours: Float = 0f
)

data class AdminReportCard(
    val type: AdminReportExportType,
    val title: String,
    val description: String,
    val rows: Int,
    val primaryMetric: String,
    val secondaryMetric: String
)

data class AdminReportExport(
    val fileName: String,
    val content: String,
    val label: String
)

data class AdminReportsState(
    val summary: AdminReportSummary = AdminReportSummary(),
    val cards: List<AdminReportCard> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val exportErrorMessage: String? = null
)

class AdminReportsViewModel(
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
        val projectMemberCounts: Map<Int, Int>,
        val taskUsers: List<TarefaUserDto>,
        val records: List<RegistoTarefaDto>
    )

    private var source: ReportsSource? = null
    private var pendingExport: AdminReportExport? = null

    var state by mutableStateOf(AdminReportsState())
        private set

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null, exportErrorMessage = null)

            val usersResult = runCatching { userRemoteDataSource.getUsers() }
            val projectsResult = projetoRepository.getProjetos()
            val tasksResult = tarefaRepository.getTarefas()
            val projectUsersResult = projetoUserRepository.getProjetoUsers()
            val projectMemberCountsResult = projetoUserRepository.getProjectMemberCounts()
            val taskUsersResult = tarefaUserRepository.getTarefaUsers()
            val recordsResult = registoTarefaRepository.getRegistos()

            if (
                usersResult.isFailure ||
                projectsResult.isFailure ||
                tasksResult.isFailure ||
                projectUsersResult.isFailure ||
                taskUsersResult.isFailure ||
                recordsResult.isFailure
            ) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os dados dos relatórios."
                )
                return@launch
            }

            val projectUsers = projectUsersResult.getOrDefault(emptyList())
            val directProjectMemberCounts = projectUsers
                .groupingBy { it.projeto_id }
                .eachCount()

            val loadedSource = ReportsSource(
                users = usersResult.getOrDefault(emptyList()),
                projects = projectsResult.getOrDefault(emptyList()),
                tasks = tasksResult.getOrDefault(emptyList()),
                projectUsers = projectUsers,
                projectMemberCounts = projectMemberCountsResult
                    .getOrDefault(emptyMap())
                    .ifEmpty { directProjectMemberCounts },
                taskUsers = taskUsersResult.getOrDefault(emptyList()),
                records = recordsResult.getOrDefault(emptyList())
            )
            source = loadedSource

            val completedTasks = loadedSource.tasks.count { it.status.isCompletedStatus() }
            val delayedTasks = loadedSource.tasks.count { it.isDelayed() }
            val totalHours = loadedSource.records.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat()
            val averageCompletion = loadedSource.records
                .map { it.taxa_conclusao }
                .averageOrZero()
                .roundToInt()

            state = AdminReportsState(
                summary = AdminReportSummary(
                    totalUsers = loadedSource.users.size,
                    totalProjects = loadedSource.projects.size,
                    totalTasks = loadedSource.tasks.size,
                    completedTasks = completedTasks,
                    delayedTasks = delayedTasks,
                    averageCompletion = averageCompletion,
                    totalHours = totalHours
                ),
                cards = buildCards(loadedSource, completedTasks, delayedTasks, totalHours),
                isLoading = false
            )
        }
    }

    fun buildExport(type: AdminReportExportType): AdminReportExport? {
        val currentSource = source

        if (currentSource == null) {
            state = state.copy(exportErrorMessage = "Os dados ainda não estão disponíveis.")
            return null
        }

        val content = when (type) {
            AdminReportExportType.Users -> buildUsersCsv(currentSource)
            AdminReportExportType.Projects -> buildProjectsCsv(currentSource)
            AdminReportExportType.Tasks -> buildTasksCsv(currentSource)
        }

        val fileName = "projecthub_${type.filePrefix}_${LocalDate.now()}.csv"
        state = state.copy(exportErrorMessage = null)

        return AdminReportExport(
            fileName = fileName,
            content = content,
            label = type.title
        ).also { pendingExport = it }
    }

    fun getPendingExport(): AdminReportExport? {
        return pendingExport
    }

    fun clearPendingExport() {
        pendingExport = null
    }

    fun setExportError(message: String) {
        state = state.copy(exportErrorMessage = message)
    }

    private fun buildCards(
        source: ReportsSource,
        completedTasks: Int,
        delayedTasks: Int,
        totalHours: Float
    ): List<AdminReportCard> {
        val activeUsers = source.users.count { it.status.normalizedStatus() in setOf("ATIVO", "ACTIVO") }
        val completedProjects = source.projects.count { it.status.isCompletedStatus() }
        val delayedProjects = source.projects.count { it.isDelayed() }
        val usersWithProjects = source.projectUsers.map { it.user_id }.distinct().size

        return listOf(
            AdminReportCard(
                type = AdminReportExportType.Users,
                title = "Estatísticas por utilizador",
                description = "Perfil, estado, projetos, tarefas concluídas e horas por utilizador.",
                rows = source.users.size,
                primaryMetric = "$activeUsers ativos",
                secondaryMetric = "$usersWithProjects com projeto"
            ),
            AdminReportCard(
                type = AdminReportExportType.Projects,
                title = "Estatísticas por projeto",
                description = "Gestor, datas, equipa, tarefas, progresso e horas por projeto.",
                rows = source.projects.size,
                primaryMetric = "$completedProjects concluídos",
                secondaryMetric = "$delayedProjects atrasados"
            ),
            AdminReportCard(
                type = AdminReportExportType.Tasks,
                title = "Estatísticas por tarefa",
                description = "Projeto, prazo, responsáveis, progresso, horas e atrasos por tarefa.",
                rows = source.tasks.size,
                primaryMetric = "$completedTasks completadas",
                secondaryMetric = "${totalHours.formatNumber()} h"
            )
        )
    }

    private fun buildUsersCsv(source: ReportsSource): String {
        val tasksById = source.tasks.mapNotNull { task -> task.id?.let { it to task } }.toMap()
        val taskIdsByUser = source.taskUsers
            .groupBy { it.user_id }
            .mapValues { (_, links) -> links.map { it.tarefa_id }.toSet() }
        val directProjectIdsByUser = source.projectUsers
            .groupBy { it.user_id }
            .mapValues { (_, links) -> links.map { it.projeto_id }.toSet() }
        val recordsByUser = source.records.groupBy { it.user_id }

        val rows = source.users
            .sortedBy { it.nome.lowercase() }
            .map { user ->
                val userId = user.id ?: 0
                val assignedTaskIds = taskIdsByUser[userId].orEmpty()
                val records = recordsByUser[userId].orEmpty()
                val completedAssignedTasks = assignedTaskIds.count { taskId ->
                    tasksById[taskId]?.status?.isCompletedStatus() == true
                }
                val projectIds = directProjectIdsByUser[userId].orEmpty() +
                    assignedTaskIds.mapNotNull { taskId -> tasksById[taskId]?.projeto_id }

                listOf(
                    user.nome,
                    user.email,
                    user.role.toDisplayLabel(),
                    user.status.toDisplayLabel(),
                    projectIds.size.toString(),
                    assignedTaskIds.size.toString(),
                    completedAssignedTasks.toString(),
                    records.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat().formatNumber()
                )
            }

        return buildCsv(
            headers = listOf(
                "Nome",
                "Email",
                "Perfil",
                "Estado",
                "Projetos",
                "Tarefas",
                "Concluídas",
                "Horas"
            ),
            rows = rows
        )
    }

    private fun buildProjectsCsv(source: ReportsSource): String {
        val usersById = source.users.mapNotNull { user -> user.id?.let { it to user.nome } }.toMap()
        val tasksByProject = source.tasks.groupBy { it.projeto_id }
        val memberCountsByProject = source.projectMemberCounts
        val recordsByTask = source.records.groupBy { it.tarefa_id }

        val rows = source.projects
            .sortedBy { it.nome.lowercase() }
            .map { project ->
                val projectId = project.id ?: 0
                val projectTasks = tasksByProject[projectId].orEmpty()
                val taskIds = projectTasks.mapNotNull { it.id }.toSet()
                val projectRecords = taskIds.flatMap { taskId -> recordsByTask[taskId].orEmpty() }
                val completedTasks = projectTasks.count { it.status.isCompletedStatus() }
                val delayedTasks = projectTasks.count { it.isDelayed() }
                val progress = if (projectTasks.isEmpty()) {
                    projectRecords.map { it.taxa_conclusao }.averageOrZero().roundToInt()
                } else {
                    ((completedTasks.toFloat() / projectTasks.size.toFloat()) * 100f).roundToInt()
                }

                listOf(
                    project.nome,
                    project.gestor_id?.let { usersById[it] }.orEmpty(),
                    project.status.toDisplayLabel(),
                    project.data_inicio.toCsvDate(),
                    project.data_fim.toCsvDate(),
                    (memberCountsByProject[projectId] ?: 0).toString(),
                    projectTasks.size.toString(),
                    completedTasks.toString(),
                    delayedTasks.toString(),
                    progress.toString(),
                    projectRecords.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat().formatNumber()
                )
            }

        return buildCsv(
            headers = listOf(
                "Projeto",
                "Gestor",
                "Estado",
                "Início",
                "Prazo",
                "Membros",
                "Tarefas",
                "Concluídas",
                "Atrasadas",
                "Progresso (%)",
                "Horas"
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
                    task.titulo,
                    projectsById[task.projeto_id].orEmpty(),
                    task.status.toDisplayLabel(),
                    task.data_fim.toCsvDate(),
                    (userCountsByTask[taskId] ?: 0).toString(),
                    records.map { it.taxa_conclusao }.averageOrZero().roundToInt().toString(),
                    records.sumOf { (it.tempo_gasto ?: 0f).toDouble() }.toFloat().formatNumber(),
                    if (task.isDelayed()) "Sim" else "Não"
                )
            }

        return buildCsv(
            headers = listOf(
                "Tarefa",
                "Projeto",
                "Estado",
                "Prazo",
                "Responsáveis",
                "Progresso (%)",
                "Horas",
                "Atrasada"
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

    private fun String.toDisplayLabel(): String {
        return when (normalizedStatus()) {
            "ADMIN" -> "Administrador"
            "GESTOR" -> "Gestor"
            "UTILIZADOR" -> "Utilizador"
            "ATIVO", "ACTIVO" -> "Ativo"
            "INATIVO", "INACTIVO" -> "Inativo"
            "PENDENTE" -> "Pendente"
            "EM_PROGRESSO" -> "Em progresso"
            "ATRASADO", "ATRASADA" -> "Atrasado"
            "CONCLUIDO", "CONCLUIDA", "COMPLETO", "COMPLETA",
            "COMPLETADO", "COMPLETADA", "FINALIZADO", "FINALIZADA" -> "Concluído"
            else -> trim()
                .lowercase()
                .replaceFirstChar { it.titlecase() }
        }
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
