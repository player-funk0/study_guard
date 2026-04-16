package com.obrynex.studyguard.summarizer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.obrynex.studyguard.ai.AIEngineManager
import com.obrynex.studyguard.ai.AIModelState
import com.obrynex.studyguard.di.ServiceLocator
import com.obrynex.studyguard.summarizer.domain.model.SummaryLevel
import com.obrynex.studyguard.summarizer.domain.usecase.SummarizeTextUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SummarizerUiState(
    val input       : String       = "",
    val level       : SummaryLevel = SummaryLevel.INTERMEDIATE,
    val result      : String?      = null,
    val isAiLoading : Boolean      = false,
    /**
     * True only when [AIEngineManager.state] is [AIModelState.Ready].
     * Kept in sync via a single collect in [init] — never set manually.
     */
    val hasAiModel  : Boolean      = false
) {
    val canSummarize get() = input.isNotBlank()
}

class SummarizerViewModel(
    private val useCase   : SummarizeTextUseCase,
    private val aiManager : AIEngineManager
) : ViewModel() {

    private val _state = MutableStateFlow(SummarizerUiState())
    val state: StateFlow<SummarizerUiState> = _state.asStateFlow()

    init {
        // Observe the shared AIEngineManager StateFlow.
        // This keeps hasAiModel in sync without triggering MediaPipe init —
        // reading a StateFlow never causes the lazy AIEngineManager to validate.
        viewModelScope.launch {
            aiManager.state.collect { engineState ->
                _state.update { it.copy(hasAiModel = engineState is AIModelState.Ready) }
            }
        }
    }

    fun onInputChanged(text: String)        = _state.update { it.copy(input = text) }
    fun onLevelChanged(level: SummaryLevel) = _state.update { it.copy(level = level) }

    /** Instant local TextRank summariser — always available, no network, no model needed. */
    fun onSummarizeClicked() {
        val text = _state.value.input.trim().ifBlank { return }
        _state.update { it.copy(result = useCase.execute(text, it.level).text) }
    }

    /**
     * AI summariser using Gemma 2B — streams tokens.
     *
     * [aiManager.getEngine] returns null unless the manager is in [AIModelState.Ready],
     * so the early-return guard is the correct and only check needed here.
     * No boolean flag, no file-stat, no duplicate readiness logic.
     */
    fun onAiSummarizeClicked() {
        val text   = _state.value.input.trim().ifBlank { return }
        val engine = aiManager.getEngine() ?: return   // not Ready — do nothing
        _state.update { it.copy(isAiLoading = true, result = "") }
        viewModelScope.launch {
            var response = ""
            engine.summarize(text, _state.value.level.label)
                .collect { token ->
                    response += token
                    _state.update { it.copy(result = response) }
                }
            _state.update { it.copy(isAiLoading = false) }
        }
    }

    fun onReset() = _state.update { SummarizerUiState(hasAiModel = _state.value.hasAiModel) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SummarizerViewModel(
                    useCase   = ServiceLocator.summarizeTextUseCase,
                    aiManager = ServiceLocator.aiEngineManager
                )
            }
        }
    }
}
