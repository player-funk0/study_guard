package com.obrynex.studyguard.tracker

import android.app.Application
import androidx.lifecycle.*
import com.obrynex.studyguard.data.db.StudySession
import com.obrynex.studyguard.data.db.StudySessionDao
import com.obrynex.studyguard.data.prefs.PrefsManager
import com.obrynex.studyguard.tracker.domain.usecase.GetStreakUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TrackerState(
    val isRunning     : Boolean            = false,
    val subject       : String             = "General",
    val elapsedMs     : Long               = 0L,
    val todayMinutes  : Int                = 0,
    val goalMinutes   : Int                = PrefsManager.DEFAULT_GOAL_MIN,
    val streak        : Int                = 0,
    val recentSessions: List<StudySession> = emptyList()
)


class TrackerViewModel constructor(
    application      : Application,
    private val dao  : StudySessionDao,
    private val getStreak: GetStreakUseCase
) : AndroidViewModel(application) {

    private val ctx = application.applicationContext

    private val _state = MutableStateFlow(TrackerState())
    val state: StateFlow<TrackerState> = _state.asStateFlow()

    private var startMs  = 0L
    private var timerJob : kotlinx.coroutines.Job? = null

    init {
        // Observe goal + sessions together — goalMinutes now updates live
        viewModelScope.launch {
            PrefsManager.dailyGoalMin(ctx)
                .combine(dao.allSessions()) { goal, sessions ->
                    Triple(goal, getStreak.execute(sessions), sessions.take(10))
                }
                .collect { (goal, streak, recent) ->
                    _state.update {
                        it.copy(goalMinutes = goal, streak = streak, recentSessions = recent)
                    }
                }
        }
        // Today's minutes observed independently for live timer accuracy
        viewModelScope.launch {
            dao.sessionsByDate(todayKey()).collect { sessions ->
                _state.update { it.copy(todayMinutes = sessions.sumOf { s -> s.durationMin }) }
            }
        }
    }

    fun onSubjectChanged(s: String) = _state.update { it.copy(subject = s) }

    fun startSession() {
        if (_state.value.isRunning) return
        startMs = System.currentTimeMillis()
        _state.update { it.copy(isRunning = true, elapsedMs = 0L) }
        timerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1_000)
                _state.update { it.copy(elapsedMs = System.currentTimeMillis() - startMs) }
            }
        }
    }

    fun stopSession() {
        if (!_state.value.isRunning) return
        timerJob?.cancel()
        val endMs  = System.currentTimeMillis()
        val durMin = ((endMs - startMs) / 60_000).toInt().coerceAtLeast(1)
        val session = StudySession(
            subject     = _state.value.subject,
            startMs     = startMs,
            endMs       = endMs,
            durationMin = durMin,
            dateKey     = todayKey()
        )
        viewModelScope.launch { dao.insert(session) }
        _state.update { it.copy(isRunning = false, elapsedMs = 0L) }
    }

    fun deleteSession(s: StudySession) = viewModelScope.launch { dao.delete(s) }

    fun formatElapsed(ms: Long): String {
        val s   = ms / 1000
        val h   = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
    }

    // Thread-safe via java.time (available from minSdk 26)
    private fun todayKey() = LocalDate.now().toString()
}

    companion object {
        val Factory: androidx.lifecycle.ViewModelProvider.Factory =
            androidx.lifecycle.viewmodel.MutableCreationExtras().let {
                androidx.lifecycle.viewmodel.viewModelFactory {
                    initializer {
                        TrackerViewModel(
                            application = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                                as android.app.Application,
                            dao          = com.obrynex.studyguard.di.ServiceLocator.studySessionDao,
                            getStreak    = com.obrynex.studyguard.di.ServiceLocator.getStreakUseCase
                        )
                    }
                }
            }
    }
