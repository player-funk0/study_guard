package com.obrynex.studyguard.wellbeing

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the addiction score calculation logic extracted as a pure function.
 */
class WellbeingAddictionScoreTest {

    private fun calcScore(actualMin: Int, limitMin: Int): Int {
        if (limitMin <= 0) return if (actualMin > 0) 100 else 0
        return ((actualMin.toFloat() / limitMin.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    @Test fun `zero actual returns zero score`() {
        assertEquals(0, calcScore(0, 240))
    }

    @Test fun `exact limit returns 100`() {
        assertEquals(100, calcScore(240, 240))
    }

    @Test fun `over limit is capped at 100`() {
        assertEquals(100, calcScore(500, 240))
    }

    @Test fun `half usage returns 50`() {
        assertEquals(50, calcScore(120, 240))
    }

    @Test fun `zero limit with usage returns 100`() {
        assertEquals(100, calcScore(60, 0))
    }

    @Test fun `zero limit with zero usage returns 0`() {
        assertEquals(0, calcScore(0, 0))
    }

    @Test fun `healthy range is below 40`() {
        assertTrue(calcScore(80, 240) < 40)
    }

    @Test fun `addicted range is 90 or above`() {
        assertTrue(calcScore(216, 240) >= 90)
    }
}
