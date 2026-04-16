package com.obrynex.studyguard.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * Single source of truth for the on-device AI model lifecycle.
 *
 * State machine:
 *
 *   NotFound ──validate()──► Validating ──gates ok──► Loading ──warmUp ok──► Ready
 *                                │                       │                      │
 *                                └──── Failed ───────────┘              releaseEngine()
 *                                                                               │
 *                                                                         (back to NotFound)
 *
 * Rules:
 *   - No other class may instantiate [LocalAiEngine] directly.
 *   - No other class may call [LocalAiEngine.close] directly.
 *   - [getEngine] returns non-null ONLY when state is [AIModelState.Ready].
 */
class AIEngineManager(private val context: Context) {

    companion object {
        /**
         * Minimum acceptable size for the Gemma 2B-IT INT4 binary.
         *
         * The canonical Kaggle download (gemma-2b-it-cpu-int4.bin) is ~1.35 GB.
         * 1.2 GB is a conservative lower bound that safely rejects:
         *   - truncated / partial downloads
         *   - wrong model files (e.g. a 400 MB placeholder)
         *   - zero-byte or near-empty corrupt writes
         *
         * This is a *documented architectural constant*, not a magic number.
         * Change it only if you switch to a different model variant.
         */
        const val MODEL_MIN_BYTES: Long = 1_200_000_000L   // 1.2 GB

        /**
         * SHA-256 digest of the canonical `gemma-2b-it-cpu-int4.bin` from Kaggle.
         *
         * How to obtain:
         *   Linux/macOS : sha256sum gemma-2b-it-cpu-int4.bin
         *   Windows     : certutil -hashfile gemma-2b-it-cpu-int4.bin SHA256
         *
         * Set this before shipping to production.
         * When null the checksum gate is skipped (size + load-test still run).
         */
        const val EXPECTED_SHA256: String? = null

        /** Relative path from getExternalFilesDir(null) — shown in error messages. */
        const val MODEL_RELATIVE_PATH = "models/${LocalAiEngine.MODEL_FILENAME}"
    }

    private val _state = MutableStateFlow<AIModelState>(AIModelState.NotFound)
    val state: StateFlow<AIModelState> = _state.asStateFlow()

    private var engine: LocalAiEngine? = null

    /** Absolute path of the expected model binary — useful for setup instructions in the UI. */
    val modelFilePath: String
        get() = File(context.getExternalFilesDir(null), MODEL_RELATIVE_PATH).absolutePath

    /**
     * Returns the live engine.
     * Non-null ONLY when [state] is [AIModelState.Ready].
     */
    fun getEngine(): LocalAiEngine? = engine

    /**
     * Runs all validation gates in sequence, then warms up the engine.
     *
     * Idempotent — exits immediately if state is already
     * [AIModelState.Validating], [AIModelState.Loading], or [AIModelState.Ready].
     *
     * Call from a ViewModel [init] block; the suspend machinery keeps everything
     * off the main thread automatically via [Dispatchers.IO] inside.
     */
    suspend fun validate() {
        when (_state.value) {
            is AIModelState.Validating,
            is AIModelState.Loading,
            is AIModelState.Ready -> return
            else -> Unit
        }

        _state.value = AIModelState.Validating

        // ── Gate phase ──────────────────────────────────────────────────────
        val failure = withContext(Dispatchers.IO) { runValidationGates() }
        if (failure != null) {
            _state.value = AIModelState.Failed(failure)
            return
        }

        // ── Load + warm-up phase ─────────────────────────────────────────────
        _state.value = AIModelState.Loading

        withContext(Dispatchers.IO) {
            runCatching {
                val eng = LocalAiEngine(context)
                eng.warmUp()   // proves the model works end-to-end, not just that it exists
                eng
            }
        }.fold(
            onSuccess = { eng ->
                engine         = eng
                _state.value   = AIModelState.Ready
            },
            onFailure = { ex ->
                _state.value   = AIModelState.Failed(ValidationFailure.LoadFailed(ex))
            }
        )
    }

    /**
     * Closes the MediaPipe session, freeing the ~1.3 GB native allocation.
     * Resets state to [AIModelState.NotFound] so [validate] can be called again.
     *
     * Call from [AiTutorViewModel.onCleared] so memory is freed as soon as the
     * AI screen leaves the back-stack.
     */
    fun releaseEngine() {
        engine?.close()
        engine       = null
        _state.value = AIModelState.NotFound
    }

    // ── Validation gates ──────────────────────────────────────────────────────

    /**
     * Runs every gate in order and returns the first [ValidationFailure],
     * or null if the model binary passes all checks.
     */
    private fun runValidationGates(): ValidationFailure? {
        val file = File(context.getExternalFilesDir(null), MODEL_RELATIVE_PATH)

        // Gate 1 — existence
        if (!file.exists()) return ValidationFailure.FileNotFound

        // Gate 2 — minimum size (documented constant above, not a magic number)
        if (file.length() < MODEL_MIN_BYTES) {
            return ValidationFailure.SizeTooSmall(
                actualBytes  = file.length(),
                minimumBytes = MODEL_MIN_BYTES
            )
        }

        // Gate 3 — SHA-256 integrity (opt-in; set EXPECTED_SHA256 before shipping)
        EXPECTED_SHA256?.let { expected ->
            val actual = sha256hex(file)
            if (!actual.equals(expected, ignoreCase = true)) {
                return ValidationFailure.ChecksumMismatch(expected, actual)
            }
        }

        return null   // ✅ all gates passed
    }

    /**
     * Computes SHA-256 of [file] in 8 MB chunks.
     * Safe for the ~1.3 GB model binary — never loads more than 8 MB at a time.
     */
    private fun sha256hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8 * 1024 * 1024)
        file.inputStream().buffered(buffer.size).use { stream ->
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
