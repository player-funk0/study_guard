package com.obrynex.studyguard.summarizer.domain.model

enum class SummaryLevel(val label: String, val ratio: Double) {
    BEGINNER    ("🟢 مختصر",         0.20),
    INTERMEDIATE("🔵 متوسط",         0.35),
    ADVANCED    ("🔴 تفصيلي",        0.50)
}

/** Local-only result — always offline, always instant. */
data class SummaryResult(val text: String)
