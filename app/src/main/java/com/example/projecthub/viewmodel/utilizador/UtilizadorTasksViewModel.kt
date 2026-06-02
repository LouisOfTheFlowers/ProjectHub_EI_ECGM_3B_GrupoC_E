package com.example.projecthub.viewmodel.utilizador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.models.ObservacaoDto
import com.example.projecthub.remote.supabase.models.ObservacaoFotoDto
import com.example.projecthub.remote.supabase.models.RegistoTarefaDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.ObservacaoFotoRepository
import com.example.projecthub.repository.ObservacaoRepository
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.RegistoTarefaRepository
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class UtilizadorTasksState(
    val tasks: List<UtilizadorTaskItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class UtilizadorTaskItem(
    val task: TarefaDto,
    val projectName: String,
    val recordsCount: Int,
    val observations: List<UtilizadorTaskObservation>
)

data class UtilizadorTaskObservation(
    val observation: ObservacaoDto,
    val record: RegistoTarefaDto,
    val photos: List<ObservacaoFotoDto>
)

class UtilizadorTasksViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository(),
    private val registoTarefaRepository: RegistoTarefaRepository = RegistoTarefaRepository(),
    private val observacaoRepository: ObservacaoRepository = ObservacaoRepository(),
    private val observacaoFotoRepository: ObservacaoFotoRepository = ObservacaoFotoRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(UtilizadorTasksState())
    val stateFlow: StateFlow<UtilizadorTasksState> = _state
    private var state: UtilizadorTasksState
        get() = _state.value
        set(value) { _state.value = value }

    fun loadTasks(userId: Int?) {
        if (userId == null) {
            state = UtilizadorTasksState(errorMessage = "Não foi possível identificar o utilizador autenticado.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val associationsResult = tarefaUserRepository.getTarefasByUser(userId)
            if (associationsResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar as tarefas do utilizador."
                )
                return@launch
            }

            val taskIds = associationsResult
                .getOrDefault(emptyList())
                .map { it.tarefa_id }
                .toSet()

            val tasksResult = tarefaRepository.getTarefas()
            val projectsResult = projetoRepository.getProjetos()
            val recordsResult = registoTarefaRepository.getRegistosByUser(userId)
            val observationsResult = observacaoRepository.getObservacoes()
            val photosResult = observacaoFotoRepository.getFotos()

            if (tasksResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os detalhes das tarefas."
                )
                return@launch
            }

            val projectsById = projectsResult.getOrDefault(emptyList())
                .mapNotNull { project -> project.id?.let { it to project } }
                .toMap()
            val records = recordsResult.getOrDefault(emptyList())
            val recordsByTask = records.groupBy { it.tarefa_id }
            val recordsById = records.mapNotNull { record -> record.id?.let { it to record } }.toMap()
            val photosByObservation = photosResult.getOrDefault(emptyList()).groupBy { it.observacao_id }
            val observationsByTask = observationsResult.getOrDefault(emptyList())
                .mapNotNull { observation ->
                    val record = recordsById[observation.registo_id] ?: return@mapNotNull null
                    record.tarefa_id to UtilizadorTaskObservation(
                        observation = observation,
                        record = record,
                        photos = observation.id?.let { photosByObservation[it].orEmpty() }.orEmpty()
                    )
                }
                .groupBy({ it.first }, { it.second })

            val taskItems = tasksResult
                .getOrDefault(emptyList())
                .filter { it.id in taskIds }
                .sortedWith(compareBy<TarefaDto> { it.status.isCompletedStatus() }.thenBy { it.data_fim.orEmpty() })
                .map { task ->
                    UtilizadorTaskItem(
                        task = task,
                        projectName = projectsById[task.projeto_id]?.nome.orEmpty(),
                        recordsCount = task.id?.let { recordsByTask[it].orEmpty().size } ?: 0,
                        observations = task.id?.let { observationsByTask[it].orEmpty() }.orEmpty()
                            .sortedByDescending { it.record.created_at.orEmpty() }
                    )
                }

            state = UtilizadorTasksState(tasks = taskItems, isLoading = false)
        }
    }

    fun addObservation(
        userId: Int?,
        taskId: Int?,
        text: String,
        photoUri: String?
    ) {
        if (userId == null || taskId == null) {
            state = state.copy(errorMessage = "Não foi possível identificar a tarefa ou o utilizador.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isSaving = true, errorMessage = null)

            val registoResult = registoTarefaRepository.createRegistoReturning(
                tarefaId = taskId,
                userId = userId,
                data = LocalDate.now().toString(),
                local = null,
                taxaConclusao = 0,
                tempoGasto = null
            )

            if (registoResult.isFailure) {
                state = state.copy(
                    isSaving = false,
                    errorMessage = registoResult.exceptionOrNull().toUserMessage(
                        fallback = "Não foi possível criar o registo da observação."
                    )
                )
                return@launch
            }

            val registoId = registoResult.getOrNull()?.id
            if (registoId == null) {
                state = state.copy(
                    isSaving = false,
                    errorMessage = "Não foi possível obter o registo criado."
                )
                return@launch
            }

            val observacaoResult = observacaoRepository.createObservacaoReturning(
                registoId = registoId,
                texto = text
            )

            if (observacaoResult.isFailure) {
                state = state.copy(
                    isSaving = false,
                    errorMessage = observacaoResult.exceptionOrNull().toUserMessage(
                        fallback = "Não foi possível guardar a observação."
                    )
                )
                return@launch
            }

            val observacaoId = observacaoResult.getOrNull()?.id
            if (!photoUri.isNullOrBlank() && observacaoId != null) {
                val photoResult = observacaoFotoRepository.createFoto(
                    observacaoId = observacaoId,
                    fotoUrl = photoUri
                )

                if (photoResult.isFailure) {
                    state = state.copy(
                        isSaving = false,
                        errorMessage = photoResult.exceptionOrNull().toUserMessage(
                            fallback = "A observação foi guardada, mas não foi possível guardar a fotografia."
                        )
                    )
                    loadTasks(userId)
                    return@launch
                }
            }

            state = state.copy(isSaving = false)
            loadTasks(userId)
        }
    }

    fun completeTask(
        userId: Int?,
        taskId: Int?,
        completionDate: String,
        location: String,
        spentHours: String
    ) {
        if (userId == null || taskId == null) {
            state = state.copy(errorMessage = "Não foi possível identificar a tarefa ou o utilizador.")
            return
        }

        val hours = spentHours.replace(",", ".").toFloatOrNull()
        if (hours == null || hours < 0f) {
            state = state.copy(errorMessage = "Indica um número válido de horas.")
            return
        }

        val parsedCompletionDate = completionDate.toLocalDateOrNull()
        if (parsedCompletionDate == null) {
            state = state.copy(errorMessage = "Indica uma data de conclusão válida.")
            return
        }

        val task = state.tasks.firstOrNull { it.task.id == taskId }?.task
        val startDate = task?.data_inicio?.toLocalDateOrNull()
        if (startDate != null && !parsedCompletionDate.isAfter(startDate)) {
            state = state.copy(errorMessage = "A data de conclusão tem de ser posterior à data de início da tarefa.")
            return
        }

        if (location.isBlank()) {
            state = state.copy(errorMessage = "Indica o local de conclusão.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isSaving = true, errorMessage = null)

            val registoResult = registoTarefaRepository.createRegisto(
                tarefaId = taskId,
                userId = userId,
                data = completionDate,
                local = location,
                taxaConclusao = 100,
                tempoGasto = hours
            )

            if (registoResult.isFailure) {
                state = state.copy(
                    isSaving = false,
                    errorMessage = registoResult.exceptionOrNull().toUserMessage(
                        fallback = "Não foi possível guardar o registo de conclusão."
                    )
                )
                return@launch
            }

            val completeResult = tarefaRepository.concluirTarefa(taskId)
            if (completeResult.isFailure) {
                state = state.copy(
                    isSaving = false,
                    errorMessage = completeResult.exceptionOrNull().toUserMessage(
                        fallback = "O registo foi guardado, mas não foi possível concluir a tarefa."
                    )
                )
                return@launch
            }

            state = state.copy(isSaving = false)
            loadTasks(userId)
        }
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

    private fun String.toLocalDateOrNull(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun Throwable?.toUserMessage(fallback: String): String {
        val message = this?.message.orEmpty()
        return when {
            message.contains("row-level security", ignoreCase = true) ->
                "Não tens permissão para guardar dados nesta tarefa."
            message.contains("permission denied", ignoreCase = true) ->
                "Não tens permissão para realizar esta ação."
            message.contains("violates", ignoreCase = true) ->
                fallback
            else -> message.takeIf { it.isNotBlank() && !it.contains("Headers:", ignoreCase = true) }
                ?: fallback
        }
    }
}
