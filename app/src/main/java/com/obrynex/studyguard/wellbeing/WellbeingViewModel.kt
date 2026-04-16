package com.obrynex.studyguard.wellbeing

import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.*
import com.obrynex.studyguard.data.prefs.PrefsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class AppUsage(val packageName: String, val label: String, val minutesUsed: Int)

data class WellbeingState(
    val hasPermission  : Boolean        = false,
    val totalScreenMin : Int            = 0,
    val limitHr        : Float          = PrefsManager.DEFAULT_SCREEN_HR,
    val topApps        : List<AppUsage> = emptyList(),
    val addictionScore : Int            = 0,
    val addictionLabel : String         = "–",
    val addictionColor : Long           = 0xFF7A7A9A,
    val overLimit      : Boolean        = false
)


class WellbeingViewModel constructor(
    private val app: Application
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(WellbeingState())
    val state: StateFlow<WellbeingState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val limitHr = PrefsManager.screenLimitHr(app).first()
            _state.update { it.copy(limitHr = limitHr) }
            refresh()
        }
    }

    fun refresh() {
        if (!hasUsagePermission()) {
            _state.update { it.copy(hasPermission = false) }
            return
        }
        try {
            val usm = app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
            }
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                cal.timeInMillis, System.currentTimeMillis()
            ) ?: emptyList()

            val pm      = app.packageManager
            val appList = stats
                .filter { it.totalTimeInForeground > 60_000 }
                .map { stat ->
                    val label = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(stat.packageName, 0)).toString()
                    } catch (e: Exception) { stat.packageName.substringAfterLast('.') }
                    AppUsage(stat.packageName, label, (stat.totalTimeInForeground / 60_000).toInt())
                }
                .sortedByDescending { it.minutesUsed }

            val totalMin = appList.sumOf { it.minutesUsed }
            val topApps  = appList.take(6)
            val limitMin = (_state.value.limitHr * 60).toInt()
            val score    = calcAddictionScore(totalMin, limitMin)
            val (label, color) = scoreLabel(score)

            _state.update {
                it.copy(
                    hasPermission  = true,
                    totalScreenMin = totalMin,
                    topApps        = topApps,
                    addictionScore = score,
                    addictionLabel = label,
                    addictionColor = color,
                    overLimit      = totalMin > limitMin
                )
            }
        } catch (e: Exception) {
            // SecurityException on MIUI or other restricted devices — treat as no permission
            _state.update { it.copy(hasPermission = false) }
        }
    }

    fun openUsageSettings() = app.startActivity(
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    )

    fun updateLimit(hr: Float) {
        viewModelScope.launch {
            PrefsManager.setScreenLimit(app, hr)
            _state.update { it.copy(limitHr = hr) }
            refresh()
        }
    }

    /**
     * Uses AppOpsManager for a reliable permission check.
     * The previous approach (queryUsageStats → check if empty) fails on MIUI 14
     * because stats can genuinely be empty even when permission is granted.
     */
    private fun hasUsagePermission(): Boolean {
        val appOps = app.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode   = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            app.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun calcAddictionScore(actualMin: Int, limitMin: Int): Int {
        if (limitMin <= 0) return if (actualMin > 0) 100 else 0
        return ((actualMin.toFloat() / limitMin.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    private fun scoreLabel(score: Int): Pair<String, Long> = when {
        score < 40 -> "صحي 🟢"       to 0xFF00C9A7L
        score < 70 -> "معتدل 🟡"      to 0xFFFFC107L
        score < 90 -> "خطر مرتفع 🟠"  to 0xFFFF8C00L
        else       -> "إدمان 🔴"      to 0xFFFF5C5CL
    }
}

    companion object {
        val Factory: androidx.lifecycle.ViewModelProvider.Factory =
            androidx.lifecycle.viewmodel.viewModelFactory {
                androidx.lifecycle.viewmodel.initializer {
                    WellbeingViewModel(
                        this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                            as android.app.Application
                    )
                }
            }
    }
