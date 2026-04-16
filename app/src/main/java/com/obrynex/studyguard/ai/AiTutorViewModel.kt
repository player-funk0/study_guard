package com.obrynex.studyguard.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.obrynex.studyguard.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val text     : String,
    val isUser   : Boolean,
    val isLoading: Boolean = false
)

data class AiTutorState(
    val messages     : List<ChatMessage> = emptyList(),
    val input        : String            = "",
    val subject      : String            = "",
    val isGenerating : Boolean           = false,
    /** Mirrors [AIEngineManager.state] — single source of truth, never duplicated. */
    val modelState   : AIModelState      = AIModelState.NotFound,
    /** Absolute path shown in setup instructions when model is missing. */
    val modelPath    : String            = ""
) {
    val isModelReady   get() = modelState is AIModelState.Ready
    val isValidating   get() = modelState is AIModelState.Validating
                            || modelState is AIModelState.Loading
    val failureMessage get() = (modelState as? AIModelState.Failed)
                                    ?.reason?.toArabicMessage()
}

class AiTutorViewModel(
    private val manager: AIEngineManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        AiTutorState(modelPath = manager.modelFilePath)
    )
    val state: StateFlow<AiTutorState> = _state.asStateFlow()

    init {
        // Keep AiTutorState.modelState in sync with the manager — one collect,
        // no local copies, no boolean flags that can drift out of sync.
        viewModelScope.launch {
            manager.state.collect { engineState ->
                _state.update { it.copy(modelState = engineState) }
            }
        }
        // Start validation (no-op if already Validating / Loading / Ready)
        viewModelScope.launch { manager.validate() }
    }

    fun onInputChanged(text: String) = _state.update { it.copy(input = text) }
    fun onSubjectChanged(s: String)  = _state.update { it.copy(subject = s) }

    fun send() {
        val text   = _state.value.input.trim().ifBlank { return }
        // Guard: only callable when state is Ready — getEngine() returns null otherwise
        val engine = manager.getEngine() ?: return
        if (_state.value.isGenerating) return

        val userMsg = ChatMessage(text, isUser = true)
        val loadMsg = ChatMessage("", isUser = false, isLoading = true)
        _state.update {
            it.copy(messages = it.messages + userMsg + loadMsg, input = "", isGenerating = true)
        }

        viewModelScope.launch {
            var response = ""
            engine.ask(text, _state.value.subject).collect { token ->
                response += token
                _state.update { s ->
                    s.copy(messages = s.messages.dropLast(1) + ChatMessage(response, isUser = false))
                }
            }
            _state.update { it.copy(isGenerating = false) }
        }
    }

    fun clear() = _state.update { it.copy(messages = emptyList()) }

    /**
     * Called automatically when the AI Tutor screen leaves the back-stack.
     *
     * Delegates to [AIEngineManager.releaseEngine] which:
     *   1. Calls [LocalAiEngine.close] → frees the ~1.35 GB MediaPipe native session
     *   2. Resets manager state to [AIModelState.NotFound]
     *
     * The next time the AI screen is opened, a new [AiTutorViewModel] is created
     * and [init] calls [manager.validate] again from scratch.
     */
    override fun onCleared() {
        super.onCleared()
        manager.releaseEngine()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { AiTutorViewModel(ServiceLocator.aiEngineManager) }
        }
    }
}
