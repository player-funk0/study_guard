package com.obrynex.studyguard.di

import android.app.Application
import com.obrynex.studyguard.ai.AIEngineManager
import com.obrynex.studyguard.data.db.AppDatabase
import com.obrynex.studyguard.data.db.StudySessionDao
import com.obrynex.studyguard.summarizer.data.repository.SummarizerRepositoryImpl
import com.obrynex.studyguard.summarizer.domain.repository.SummarizerRepository
import com.obrynex.studyguard.summarizer.domain.usecase.SummarizeTextUseCase
import com.obrynex.studyguard.tracker.domain.usecase.GetStreakUseCase

/**
 * Manual dependency injection container — replaces Hilt.
 * All properties are lazy; nothing is created until first access.
 */
object ServiceLocator {

    private lateinit var app: Application

    fun init(application: Application) {
        app = application
    }

    val database: AppDatabase by lazy { AppDatabase.get(app) }

    val studySessionDao: StudySessionDao by lazy { database.studySessionDao() }

    val summarizerRepository: SummarizerRepository by lazy { SummarizerRepositoryImpl() }

    val summarizeTextUseCase: SummarizeTextUseCase by lazy {
        SummarizeTextUseCase(summarizerRepository)
    }

    val getStreakUseCase: GetStreakUseCase by lazy { GetStreakUseCase() }

    /**
     * Single source of truth for the on-device AI engine lifecycle.
     *
     * Design intent
     * ─────────────
     * • ViewModels must access the AI engine exclusively through this manager —
     *   never by instantiating [LocalAiEngine] directly.
     * • The manager's [AIEngineManager.state] is the only authoritative signal
     *   for engine readiness; no ViewModel or screen should maintain its own copy.
     * • The lazy delegate here means [AIEngineManager] is instantiated (not the
     *   engine itself) the first time any code touches this property — which is
     *   when [AiTutorViewModel] is first created, i.e. when the user opens the AI
     *   screen.  MediaPipe native libs and the 1.35 GB model are NOT loaded until
     *   [AIEngineManager.validate] completes.
     */
    val aiEngineManager: AIEngineManager by lazy { AIEngineManager(app) }
}
