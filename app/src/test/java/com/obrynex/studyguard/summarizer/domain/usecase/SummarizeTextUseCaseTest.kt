package com.obrynex.studyguard.summarizer.domain.usecase

import com.obrynex.studyguard.summarizer.domain.model.SummaryLevel
import com.obrynex.studyguard.summarizer.domain.model.SummaryResult
import com.obrynex.studyguard.summarizer.domain.repository.SummarizerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SummarizeTextUseCaseTest {

    private lateinit var repository: SummarizerRepository
    private lateinit var useCase: SummarizeTextUseCase

    @Before fun setUp() {
        repository = mockk()
        useCase    = SummarizeTextUseCase(repository)
    }

    @Test fun `execute returns summary from repository`() {
        val text = "The quick brown fox jumps. It leaps over the lazy dog. Animals are amazing."
        every { repository.summarize(text, SummaryLevel.INTERMEDIATE) } returns SummaryResult("Fox jumps over dog.")
        val result = useCase.execute(text, SummaryLevel.INTERMEDIATE)
        assertEquals("Fox jumps over dog.", result.text)
    }

    @Test fun `execute trims input before passing to repository`() {
        val trimmed = "Some text here."
        every { repository.summarize(trimmed, SummaryLevel.BEGINNER) } returns SummaryResult("Some text.")
        useCase.execute("  Some text here.  ", SummaryLevel.BEGINNER)
        verify { repository.summarize(trimmed, SummaryLevel.BEGINNER) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `execute throws on blank input`() {
        useCase.execute("   ", SummaryLevel.INTERMEDIATE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `execute throws on empty input`() {
        useCase.execute("", SummaryLevel.ADVANCED)
    }

    @Test fun `execute passes correct level to repository`() {
        val text = "Some valid text for summarization."
        every { repository.summarize(text, SummaryLevel.ADVANCED) } returns SummaryResult("Valid text.")
        useCase.execute(text, SummaryLevel.ADVANCED)
        verify { repository.summarize(text, SummaryLevel.ADVANCED) }
    }
}
