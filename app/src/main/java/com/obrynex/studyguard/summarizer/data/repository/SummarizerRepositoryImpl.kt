package com.obrynex.studyguard.summarizer.data.repository

import com.obrynex.studyguard.summarizer.data.local.TextRankEngine
import com.obrynex.studyguard.summarizer.domain.model.SummaryLevel
import com.obrynex.studyguard.summarizer.domain.model.SummaryResult
import com.obrynex.studyguard.summarizer.domain.repository.SummarizerRepository

class SummarizerRepositoryImpl constructor() : SummarizerRepository {
    override fun summarize(text: String, level: SummaryLevel): SummaryResult =
        SummaryResult(TextRankEngine.summarize(text, level))
}
