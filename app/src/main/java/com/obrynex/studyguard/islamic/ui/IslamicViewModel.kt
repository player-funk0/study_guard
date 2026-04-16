package com.obrynex.studyguard.islamic.ui

import androidx.lifecycle.ViewModel
import com.obrynex.studyguard.islamic.data.IslamicDataSource
import com.obrynex.studyguard.islamic.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class IslamicTab(val label: String, val emoji: String) {
    HADITHS ("الأحاديث", "📜"),
    ADHKAR  ("الأذكار",  "🤲"),
    TASBIH  ("التسبيح",  "📿")
}

data class IslamicUiState(
    val activeTab           : IslamicTab          = IslamicTab.HADITHS,
    val hadithCategory      : HadithCategory?     = null,
    val dhikrCategory       : DhikrCategory?      = null,
    val expandedHadithId    : Int?                = null,
    val expandedDhikrId     : Int?                = null,
    val tasbihItemId        : Int                 = 1,
    val tasbihCount         : Int                 = 0,
    val allHadiths          : List<Hadith>        = IslamicDataSource.hadiths,
    val allAdhkar           : List<Dhikr>         = IslamicDataSource.adhkar,
    val tasbihItems         : List<TasbihItem>    = IslamicDataSource.tasbihItems
) {
    val filteredHadiths get() = if (hadithCategory == null) allHadiths
                                else allHadiths.filter { it.category == hadithCategory }
    val filteredAdhkar  get() = if (dhikrCategory == null) allAdhkar
                                else allAdhkar.filter { it.category == dhikrCategory }
    val currentTasbih   get() = tasbihItems.find { it.id == tasbihItemId } ?: tasbihItems.first()
    val tasbihProgress  get() = (tasbihCount.toFloat() / currentTasbih.target.toFloat()).coerceIn(0f, 1f)
    val tasbihDone      get() = tasbihCount >= currentTasbih.target
}


class IslamicViewModel constructor() : ViewModel() {

    private val _state = MutableStateFlow(IslamicUiState())
    val state: StateFlow<IslamicUiState> = _state.asStateFlow()

    fun onTabChanged(tab: IslamicTab)                  = _state.update { it.copy(activeTab = tab) }
    fun onHadithCategoryChanged(cat: HadithCategory?)  = _state.update { it.copy(hadithCategory = cat) }
    fun onDhikrCategoryChanged(cat: DhikrCategory?)    = _state.update { it.copy(dhikrCategory = cat) }
    fun toggleHadith(id: Int)                          = _state.update { it.copy(expandedHadithId = if (it.expandedHadithId == id) null else id) }
    fun toggleDhikr(id: Int)                           = _state.update { it.copy(expandedDhikrId  = if (it.expandedDhikrId  == id) null else id) }
    fun onTasbihItemSelected(id: Int)                  = _state.update { it.copy(tasbihItemId = id, tasbihCount = 0) }
    fun onTasbihTap()                                  = _state.update { if (!it.tasbihDone) it.copy(tasbihCount = it.tasbihCount + 1) else it }
    fun onTasbihReset()                                = _state.update { it.copy(tasbihCount = 0) }
}

    companion object {
        val Factory: androidx.lifecycle.ViewModelProvider.Factory =
            androidx.lifecycle.viewmodel.viewModelFactory {
                androidx.lifecycle.viewmodel.initializer { IslamicViewModel() }
            }
    }
