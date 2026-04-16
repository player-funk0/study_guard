package com.obrynex.studyguard.ai

/**
 * Exhaustive state machine for the on-device AI engine.
 * [AIEngineManager] is the only class allowed to emit these states.
 */
sealed class AIModelState {

    /** No model file found at the expected path — initial state. */
    object NotFound : AIModelState()

    /** File found; running existence / size / checksum gates. */
    object Validating : AIModelState()

    /** All validation gates passed; MediaPipe LlmInference is initialising. */
    object Loading : AIModelState()

    /** Engine is live and ready to accept prompts. */
    object Ready : AIModelState()

    /** At least one gate failed — inspect [reason] for the exact cause. */
    data class Failed(val reason: ValidationFailure) : AIModelState()
}

/** Describes exactly which validation gate failed and why. */
sealed class ValidationFailure {

    /** Model binary does not exist at the expected path. */
    object FileNotFound : ValidationFailure()

    /**
     * File exists but is too small to be a valid Gemma model.
     * Most likely a truncated or partial download.
     */
    data class SizeTooSmall(
        val actualBytes  : Long,
        val minimumBytes : Long
    ) : ValidationFailure()

    /**
     * File size is plausible but SHA-256 does not match [AIEngineManager.EXPECTED_SHA256].
     * Could indicate a corrupted download or the wrong model version.
     */
    data class ChecksumMismatch(
        val expected : String,
        val actual   : String
    ) : ValidationFailure()

    /**
     * MediaPipe raised an exception during [LocalAiEngine.warmUp].
     * The binary exists and has the right size/hash, but the model itself
     * is structurally invalid or incompatible with this MediaPipe version.
     */
    data class LoadFailed(val cause: Throwable) : ValidationFailure()

    /** Arabic user-facing message for display in the UI. */
    fun toArabicMessage(): String = when (this) {
        is FileNotFound     ->
            "ملف النموذج غير موجود.\nتأكد من نقل الملف إلى:\n${AIEngineManager.MODEL_RELATIVE_PATH}"
        is SizeTooSmall     ->
            "حجم الملف ${actualBytes / 1_000_000} MB — أصغر من الحد الأدنى ${minimumBytes / 1_000_000} MB.\n" +
            "يبدو أن التحميل لم يكتمل. أعد تحميل النموذج من Kaggle."
        is ChecksumMismatch ->
            "الملف تالف أو إصدار مختلف عن المتوقع.\nأعد تحميل النموذج من Kaggle."
        is LoadFailed       ->
            "فشل تحميل النموذج في MediaPipe:\n${cause.message}"
    }
}
