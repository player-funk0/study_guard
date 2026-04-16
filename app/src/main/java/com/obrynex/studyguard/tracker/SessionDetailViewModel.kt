package com.obrynex.studyguard.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.obrynex.studyguard.data.db.StudySession
import com.obrynex.studyguard.data.db.StudySessionDao
import com.obrynex.studyguard.di.ServiceLocator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SessionDetailViewModel(private val dao: StudySessionDao) : ViewModel() {

    fun sessionById(id: Long): Flow<StudySession?> =
        dao.allSessions().map { list -> list.find { it.id == id } }

    fun delete(session: StudySession) = viewModelScope.launch { dao.delete(session) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SessionDetailViewModel(ServiceLocator.studySessionDao) }
        }
    }
}
