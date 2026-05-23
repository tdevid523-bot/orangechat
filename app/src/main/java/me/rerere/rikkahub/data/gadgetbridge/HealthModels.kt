
package me.rerere.rikkahub.data.gadgetbridge

import java.time.LocalDate

data class DailySummary(
    val timestamp: Long,
    val date: LocalDate,
    val steps: Int,
    val hrResting: Int?,
    val hrMax: Int?,
    val hrMin: Int?,
    val hrAvg: Int?,
    val stressAvg: Int?,
    val calories: Int?,
    val spo2Avg: Int?,
)

data class ActivitySample(
    val timestamp: Long,
    val heartRate: Int?,
    val steps: Int?,
    val stress: Int?,
    val spo2: Int?,
    val rawIntensity: Int?,
)

data class SleepStage(
    val timestamp: Long,
    val stage: Int,
) {
    val stageName: String
        get() = when (stage) {
            2 -> "浅睡"
            3 -> "深睡"
            4 -> "REM"
            else -> "未知"
        }
}

data class HealthUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val dbFileExists: Boolean = true,
    val currentHeartRate: Int? = null,
    val dailySummaries7: List<DailySummary> = emptyList(),
    val dailySummaries30: List<DailySummary> = emptyList(),
    val lastNightSleepStages: List<SleepStage> = emptyList(),
    val latestSpo2: Int? = null,
    val latestStress: Int? = null,
    val todaySteps: Int = 0,
    val todayCalories: Int? = null,
    val stepsRange: StepsRange = StepsRange.SEVEN_DAYS,
)

enum class StepsRange {
    SEVEN_DAYS,
    THIRTY_DAYS,
}
