package me.rerere.rikkahub.data.ai.tools

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import me.rerere.ai.core.InputSchema
import me.rerere.ai.core.Tool
import me.rerere.ai.ui.UIMessagePart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createAlarmTool(context: Context): Tool = Tool(
    name = "manage_alarm",
    description = "Manage alarms on the user's device. " +
        "Can set new alarms, get the next scheduled alarm info, show all alarms, and delete alarms. " +
        "Alarms are managed through the system clock app.",
    parameters = {
        InputSchema.Obj(
            properties = buildJsonObject {
                putJsonObject("action") {
                    put("type", "string")
                    put("description", "Action to perform: 'set' (create alarm), 'next' (get next alarm), 'show_alarms' (open clock app), 'delete' (delete alarm)")
                    put("enum", buildJsonArray { add(JsonPrimitive("set")); add(JsonPrimitive("next")); add(JsonPrimitive("show_alarms")); add(JsonPrimitive("delete")) })
                }
                putJsonObject("hour") {
                    put("type", "integer")
                    put("description", "Hour in 24-hour format (0-23). Required for 'set' action.")
                }
                putJsonObject("minute") {
                    put("type", "integer")
                    put("description", "Minute (0-59). Required for 'set' action.")
                }
                putJsonObject("label") {
                    put("type", "string")
                    put("description", "A label/name for the alarm (optional, for 'set' action)")
                }
                putJsonObject("delete_all") {
                    put("type", "boolean")
                    put("description", "If true for 'delete' action, dismiss all alarms (optional)")
                }
            },
            required = listOf("action")
        )
    },
    execute = { args ->
        val params = args.jsonObject
        val action = params["action"]?.jsonPrimitive?.content ?: ""
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        when (action) {
            "set" -> {
                val hour = params["hour"]?.jsonPrimitive?.content?.toIntOrNull()
                val minute = params["minute"]?.jsonPrimitive?.content?.toIntOrNull()
                val label = params["label"]?.jsonPrimitive?.content ?: ""

                if (hour == null || minute == null) {
                    return@Tool listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", "Missing required parameters for 'set': hour and minute")
                        }.toString()
                    ))
                }

                if (hour !in 0..23 || minute !in 0..59) {
                    return@Tool listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", "Invalid time: hour must be 0-23, minute must be 0-59")
                        }.toString()
                    ))
                }

                try {
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, minute)
                        if (label.isNotBlank()) {
                            putExtra(AlarmClock.EXTRA_MESSAGE, label)
                        }
                        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    val activities = context.packageManager.queryIntentActivities(intent, 0)
                    if (activities.isNullOrEmpty()) {
                        return@Tool listOf(UIMessagePart.Text(
                            buildJsonObject {
                                put("success", false)
                                put("error", "No clock app found that supports setting alarms")
                            }.toString()
                        ))
                    }

                    context.startActivity(intent)

                    val displayHour = String.format("%02d", hour)
                    val displayMinute = String.format("%02d", minute)

                    listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", true)
                            put("action", "set")
                            put("alarm_time", "$displayHour:$displayMinute")
                            put("label", label)
                            put("message", "Alarm set for $displayHour:$displayMinute${if (label.isNotBlank()) " ($label)" else ""}")
                        }.toString()
                    ))
                } catch (e: Exception) {
                    listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", e.message ?: "Failed to set alarm")
                        }.toString()
                    ))
                }
            }

            "next" -> {
                try {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val nextAlarmClock = alarmManager.nextAlarmClock

                    if (nextAlarmClock != null) {
                        val triggerTime = nextAlarmClock.triggerTime
                        val timeUntil = triggerTime - System.currentTimeMillis()
                        val hoursUntil = timeUntil / (1000 * 60 * 60)
                        val minutesUntil = (timeUntil / (1000 * 60)) % 60

                        listOf(UIMessagePart.Text(
                            buildJsonObject {
                                put("success", true)
                                put("action", "next")
                                put("trigger_time", dateFormat.format(Date(triggerTime)))
                                put("timestamp", triggerTime)
                                put("hours_until", hoursUntil)
                                put("minutes_until", minutesUntil)
                                put("message", "Next alarm in ${hoursUntil}h ${minutesUntil}m (${dateFormat.format(Date(triggerTime))})")
                            }.toString()
                        ))
                    } else {
                        listOf(UIMessagePart.Text(
                            buildJsonObject {
                                put("success", true)
                                put("action", "next")
                                put("message", "No upcoming alarms found")
                            }.toString()
                        ))
                    }
                } catch (e: Exception) {
                    listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", e.message ?: "Failed to get next alarm")
                        }.toString()
                    ))
                }
            }

            "show_alarms" -> {
                try {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val activities = context.packageManager.queryIntentActivities(intent, 0)
                    if (activities.isNullOrEmpty()) {
                        return@Tool listOf(UIMessagePart.Text(
                            buildJsonObject {
                                put("success", false)
                                put("error", "No clock app found that supports showing alarms")
                            }.toString()
                        ))
                    }
                    context.startActivity(intent)
                    listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", true)
                            put("action", "show_alarms")
                            put("message", "Opened clock app to show alarms")
                        }.toString()
                    ))
                } catch (e: Exception) {
                    listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", e.message ?: "Failed to show alarms")
                        }.toString()
                    ))
                }
            }

            "delete" -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    return@Tool listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", "Deleting alarms requires Android 12 (API 31) or higher")
                        }.toString()
                    ))
                }
                val deleteAll = params["delete_all"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                try {
                    if (deleteAll) {
                        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
                            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_ALL)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val activities = context.packageManager.queryIntentActivities(intent, 0)
                        if (activities.isNullOrEmpty()) {
                            return@Tool listOf(UIMessagePart.Text(
                                buildJsonObject {
                                    put("success", false)
                                    put("error", "No clock app found that supports dismissing alarms")
                                }.toString()
                            ))
                        }
                        context.startActivity(intent)
                        listOf(UIMessagePart.Text(
                            buildJsonObject {
                                put("success", true)
                                put("action", "delete")
                                put("message", "Requested to dismiss all alarms")
                            }.toString()
                        ))
                    } else {
                        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
                            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_NEXT)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val activities = context.packageManager.queryIntentActivities(intent, 0)
                        if (activities.isNullOrEmpty()) {
                            return@Tool listOf(UIMessagePart.Text(
                                buildJsonObject {
                                    put("success", false)
                                    put("error", "No clock app found that supports dismissing alarms. Try opening the clock app manually.")
                                }.toString()
                            ))
                        }
                        context.startActivity(intent)
                        listOf(UIMessagePart.Text(
                            buildJsonObject {
                                put("success", true)
                                put("action", "delete")
                                put("message", "Requested to dismiss the next alarm")
                            }.toString()
                        ))
                    }
                } catch (e: Exception) {
                    listOf(UIMessagePart.Text(
                        buildJsonObject {
                            put("success", false)
                            put("error", e.message ?: "Failed to delete alarm")
                        }.toString()
                    ))
                }
            }

            else -> {
                listOf(UIMessagePart.Text(
                    buildJsonObject {
                        put("success", false)
                        put("error", "Unknown action: $action. Supported actions: set, next, show_alarms, delete")
                    }.toString()
                ))
            }
        }
    }
)
