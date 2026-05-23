package me.rerere.rikkahub.data.datastore

import kotlinx.serialization.Serializable

@Serializable
data class ProactiveMessageSetting(
    val enabled: Boolean = false,
    val minIntervalMinutes: Int = 30,
    val maxIntervalMinutes: Int = 90,
    val assistantId: String = "",
)
