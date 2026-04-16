package com.obrynex.studyguard.tracker.domain.usecase

import com.obrynex.studyguard.data.db.StudySession
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class GetStreakUseCaseTest {

    private lateinit var useCase: GetStreakUseCase
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Before fun setUp() { useCase = GetStreakUseCase() }

    private fun session(dateKey: String) = StudySession(
        subject = "Test", startMs = 0L, endMs = 3_600_000L,
        durationMin = 60, dateKey = dateKey
    )

    private fun today()     = fmt.format(Date())
    private fun daysAgo(n: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -n) }
        return fmt.format(cal.time)
    }

    @Test fun `empty sessions returns zero`() {
        assertEquals(0, useCase.execute(emptyList()))
    }

    @Test fun `single session today returns 1`() {
        assertEquals(1, useCase.execute(listOf(session(today()))))
    }

    @Test fun `consecutive days returns correct streak`() {
        val sessions = listOf(today(), daysAgo(1), daysAgo(2), daysAgo(3)).map { session(it) }
        assertEquals(4, useCase.execute(sessions))
    }

    @Test fun `gap in days breaks streak`() {
        val sessions = listOf(today(), daysAgo(1), daysAgo(3)).map { session(it) }
        assertEquals(2, useCase.execute(sessions))
    }

    @Test fun `old sessions with no recent activity returns zero`() {
        val sessions = listOf(daysAgo(5), daysAgo(6)).map { session(it) }
        assertEquals(0, useCase.execute(sessions))
    }

    @Test fun `streak starting yesterday still counts`() {
        val sessions = listOf(daysAgo(1), daysAgo(2), daysAgo(3)).map { session(it) }
        assertEquals(3, useCase.execute(sessions))
    }

    @Test fun `duplicate dates on same day counted once`() {
        val sessions = listOf(today(), today(), daysAgo(1)).map { session(it) }
        assertEquals(2, useCase.execute(sessions))
    }
}
