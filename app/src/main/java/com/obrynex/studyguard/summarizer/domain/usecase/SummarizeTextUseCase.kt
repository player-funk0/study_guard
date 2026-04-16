package com.obrynex.studyguard.summarizer.domain.usecase

import com.obrynex.studyguard.summarizer.domain.model.SummaryLevel
import com.obrynex.studyguard.summarizer.domain.model.SummaryResult
import com.obrynex.studyguard.summarizer.domain.repository.SummarizerRepository

class SummarizeTextUseCase constructor(
    private val repository: SummarizerRepository
) {
    /** Synchronous — returns instantly using local TextRank engine, zero network. */
    fun execute(text: String, level: SummaryLevel): SummaryResult {
        require(text.isNotBlank()) { "Input text must not be blank" }
        return repository.summarize(text.trim(), level)
    }
}
