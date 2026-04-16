package com.obrynex.studyguard.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Thin wrapper around MediaPipe [LlmInference] for Gemma 2B-IT on-device.
 *
 * Lifecycle is managed entirely by [AIEngineManager]:
 *   - Instantiated inside [AIEngineManager.validate] after all gates pass.
 *   - Closed via [AIEngineManager.releaseEngine] when the AI screen is dismissed.
 *
 * Model setup (one-time, free):
 *   1. https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-cpu-int4
 *   2. Download  gemma-2b-it-cpu-int4.bin  (~1.35 GB)
 *   3. adb push gemma-2b-it-cpu-int4.bin \
 *        /sdcard/Android/data/com.obrynex.studyguard/files/models/
 */
class LocalAiEngine(private val context: Context) {

    companion object {
        const val MODEL_FILENAME = "gemma-2b-it-cpu-int4.bin"
        const val MAX_TOKENS     = 1024

        /**
         * Canonical model path for this device.
         * Used by both [LocalAiEngine] and [AIEngineManager] — single definition.
         */
        fun modelFile(context: Context): File =
            File(context.getExternalFilesDir(null), "models/$MODEL_FILENAME")
    }

    val modelFile: File get() = Companion.modelFile(context)

    private var inference: LlmInference? = null

    private fun getOrCreate(): LlmInference = inference ?: run {
        val opts = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(MAX_TOKENS)
            .setTopK(40)
            .setTemperature(0.7f)
            .setRandomSeed(42)
            .build()
        LlmInference.createFromOptions(context, opts).also { inference = it }
    }

    /**
     * Proves the engine works end-to-end with a minimal single-turn inference.
     *
     * Called once by [AIEngineManager] during the [AIModelState.Loading] phase.
     * Throws if the binary is structurally invalid or incompatible with this
     * MediaPipe version — propagated as [ValidationFailure.LoadFailed].
     *
     * Why a real inference and not just [getOrCreate]?
     * [LlmInference.createFromOptions] can succeed on a corrupt model that only
     * fails at inference time. The warm-up catches that class of failures here,
     * not mid-conversation.
     */
    suspend fun warmUp(): Unit = withContext(Dispatchers.IO) {
        val eng  = getOrCreate()
        val done = CompletableDeferred<Unit>()
        eng.generateResponseAsync(buildGemmaPrompt("test")) { _, finished ->
            if (finished && !done.isCompleted) done.complete(Unit)
        }
        done.await()
    }

    /**
     * Streams AI response token-by-token.
     *
     * Bug fixed: previous version called `withContext(Dispatchers.IO)` *inside*
     * `callbackFlow`, which is incorrect (callbackFlow's lambda is not a suspend
     * context that can switch dispatchers mid-flight).
     * Correct pattern: confine execution with `flowOn(Dispatchers.IO)` *outside*.
     */
    fun generate(prompt: String): Flow<String> = callbackFlow {
        val eng        = getOrCreate()
        val fullPrompt = buildGemmaPrompt(prompt)

        eng.generateResponseAsync(fullPrompt) { partialResult, finished ->
            trySend(partialResult ?: "")
            if (finished) close()
        }

        awaitClose()
    }.flowOn(Dispatchers.IO)

    /** AI summarise — Arabic output, streaming. */
    fun summarize(text: String, levelLabel: String): Flow<String> = generate(
        """أنت مساعد دراسي ذكي. لخّص النص التالي باللغة العربية بأسلوب $levelLabel.
اكتب الملخص فقط بدون أي مقدمة أو تعليق.

النص:
$text"""
    )

    /** AI study tutor — answers questions in Arabic. */
    fun ask(question: String, subject: String = ""): Flow<String> = generate(
        """أنت مدرس ذكي متخصص${if (subject.isNotBlank()) " في $subject" else ""}.
أجب على السؤال التالي بأسلوب واضح ومبسط باللغة العربية.
إذا كان السؤال يحتاج خطوات، اشرحها بالترتيب.

السؤال: $question"""
    )

    private fun buildGemmaPrompt(userPrompt: String): String =
        "<start_of_turn>user\n$userPrompt<end_of_turn>\n<start_of_turn>model\n"

    /** Called exclusively by [AIEngineManager.releaseEngine]. */
    fun close() {
        inference?.close()
        inference = null
    }
}
