package me.rerere.rikkahub.data.gadgetbridge

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.util.Log
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val TAG = "GadgetbridgeReader"

object GadgetbridgeDbPath {
    /** 使用 Environment API 获取主路径 */
    val DB_PATH: String
        get() = File(
            Environment.getExternalStorageDirectory(),
            "Download/手环/Gadgetbridge.db"
        ).absolutePath

    /** 列出所有可能的路径变体，用于搜索和诊断 */
    val POSSIBLE_PATHS: List<String>
        get() = listOf(
            // 主路径：使用 Environment API
            DB_PATH,
            // 常见硬编码路径变体
            "/sdcard/Download/手环/Gadgetbridge.db",
            "/storage/emulated/0/Download/手环/Gadgetbridge.db",
            // 有些设备的 Download 目录可能不同
            "/sdcard/下载/手环/Gadgetbridge.db",
            "/storage/emulated/0/下载/手环/Gadgetbridge.db",
            File(Environment.getExternalStorageDirectory(), "下载/手环/Gadgetbridge.db").absolutePath,
        )
}

object GadgetbridgeReader {

    fun dbFileExists(): Boolean {
        // 尝试所有可能的路径
        for (path in GadgetbridgeDbPath.POSSIBLE_PATHS) {
            try {
                val file = File(path)
                Log.d(TAG, "检查数据库文件: $path, exists=${file.exists()}, length=${if (file.exists()) file.length() else 0}")
                if (file.exists() && file.length() > 0) {
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查数据库文件失败: $path", e)
            }
        }
        return false
    }

    /**
     * 获取实际存在的数据库文件路径
     */
    private fun findDbPath(): String? {
        for (path in GadgetbridgeDbPath.POSSIBLE_PATHS) {
            try {
                val file = File(path)
                if (file.exists() && file.length() > 0) {
                    Log.d(TAG, "找到数据库文件: $path")
                    return path
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun <T> withDatabase(block: (SQLiteDatabase) -> T): Result<T> {
        val dbPath = findDbPath() ?: return Result.failure(
            IllegalStateException("Gadgetbridge 数据库文件不存在")
        )
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(
                dbPath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            Result.success(block(db))
        } catch (e: Exception) {
            Log.e(TAG, "打开数据库失败: $dbPath", e)
            Result.failure(e)
        } finally {
            db?.close()
        }
    }

    fun readDailySummaries(days: Int): List<DailySummary> {
        return withDatabase { db ->
            val now = LocalDate.now()
            val startTime = now.minusDays(days.toLong())
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val summaries = mutableListOf<DailySummary>()
            val cursor = db.query(
                "XIAOMI_DAILY_SUMMARY_SAMPLE",
                arrayOf("TIMESTAMP", "STEPS", "HR_RESTING", "HR_MAX", "HR_MIN", "HR_AVG", "STRESS_AVG", "CALORIES", "SPO2_AVG"),
                "TIMESTAMP >= ?",
                arrayOf(startTime.toString()),
                null, null, "TIMESTAMP ASC"
            )
            cursor.use {
                while (it.moveToNext()) {
                    val timestamp = it.getLong(0)
                    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                    summaries.add(DailySummary(timestamp, date, it.getInt(1), getIntOrNull(it, 2), getIntOrNull(it, 3), getIntOrNull(it, 4), getIntOrNull(it, 5), getIntOrNull(it, 6), getIntOrNull(it, 7), getIntOrNull(it, 8)))
                }
            }
            summaries
        }.getOrDefault(emptyList())
    }

    fun readLatestActivitySample(): ActivitySample? {
        return withDatabase { db ->
            val cursor = db.query("XIAOMI_ACTIVITY_SAMPLE", arrayOf("TIMESTAMP", "HEART_RATE", "STEPS", "STRESS", "SPO2", "RAW_INTENSITY"), null, null, null, null, "TIMESTAMP DESC", "1")
            cursor.use {
                if (it.moveToFirst()) ActivitySample(it.getLong(0), getIntOrNull(it, 1), getIntOrNull(it, 2), getIntOrNull(it, 3), getIntOrNull(it, 4), getIntOrNull(it, 5)) else null
            }
        }.getOrDefault(null)
    }

    fun readLastNightSleepStages(): List<SleepStage> {
        return withDatabase { db ->
            val now = LocalDate.now()
            val start = now.minusDays(1).atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = now.atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val stages = mutableListOf<SleepStage>()
            val cursor = db.query("XIAOMI_SLEEP_STAGE_SAMPLE", arrayOf("TIMESTAMP", "STAGE"), "TIMESTAMP >= ? AND TIMESTAMP <= ?", arrayOf(start.toString(), end.toString()), null, null, "TIMESTAMP ASC")
            cursor.use {
                while (it.moveToNext()) { stages.add(SleepStage(it.getLong(0), it.getInt(1))) }
            }
            stages
        }.getOrDefault(emptyList())
    }

    fun readLatestSpo2AndStress(): Pair<Int?, Int?> {
        return withDatabase { db ->
            val startSec = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
            var spo2: Int? = null
            var stress: Int? = null
            val c1 = db.query("XIAOMI_ACTIVITY_SAMPLE", arrayOf("SPO2"), "TIMESTAMP >= ? AND SPO2 IS NOT NULL AND SPO2 > 0", arrayOf(startSec.toString()), null, null, "TIMESTAMP DESC", "1")
            c1.use { if (it.moveToFirst()) spo2 = getIntOrNull(it, 0) }
            val c2 = db.query("XIAOMI_ACTIVITY_SAMPLE", arrayOf("STRESS"), "TIMESTAMP >= ? AND STRESS IS NOT NULL AND STRESS > 0", arrayOf(startSec.toString()), null, null, "TIMESTAMP DESC", "1")
            c2.use { if (it.moveToFirst()) stress = getIntOrNull(it, 0) }
            Pair(spo2, stress)
        }.getOrDefault(null to null)
    }

    private fun getIntOrNull(cursor: android.database.Cursor, index: Int): Int? {
        return try { if (cursor.isNull(index)) null else cursor.getInt(index) } catch (_: Exception) { null }
    }
}