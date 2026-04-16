package com.obrynex.studyguard.summarizer.domain.repository

import com.obrynex.studyguard.summarizer.domain.model.SummaryLevel
import com.obrynex.studyguard.summarizer.domain.model.SummaryResult

interface SummarizerRepository {
    /** Local TextRank summarization — instant, always offline */
    fun summarize(text: String, level: SummaryLevel): SummaryResult
}
