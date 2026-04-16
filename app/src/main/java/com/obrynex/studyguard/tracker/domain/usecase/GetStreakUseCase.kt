package com.obrynex.studyguard.tracker.domain.usecase

import com.obrynex.studyguard.data.db.StudySession
import java.time.LocalDate

/**
 * Domain use-case: counts consecutive study days ending today or yesterday.
 * Uses java.time.LocalDate (thread-safe, available from API 26).
 */
class GetStreakUseCase constructor() {

    fun execute(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0

        val distinctDays = sessions.map { it.dateKey }.distinct().sortedDescending()
        val today        = LocalDate.now().toString()
        val yesterday    = LocalDate.now().minusDays(1).toString()

        if (distinctDays.first() != today && distinctDays.first() != yesterday) return 0

        var streak = 1
        for (i in 0 until distinctDays.size - 1) {
            val prevDay = LocalDate.parse(distinctDays[i]).minusDays(1).toString()
            if (prevDay == distinctDays[i + 1]) streak++ else break
        }
        return streak
    }
}
