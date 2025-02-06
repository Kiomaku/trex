package me.barsam.gunMaxEasyPlugin

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object LogManager {
    private lateinit var logConfig: FileConfiguration
    private lateinit var logFile: File

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun initialize(plugin: GunMaxEasyPlugin) {
        logFile = File(plugin.dataFolder, "mob-log.yml")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        logConfig = YamlConfiguration.loadConfiguration(logFile)
    }

    fun logMobKill(playerName: String, mobType: String, reward: Int) {
        val date = dateFormat.format(Date())
        val logEntry = mapOf(
            "action" to "MobKill",
            "date" to date,
            "player" to playerName,
            "mobType" to mobType,
            "reward" to reward
        )
        appendToLog(logEntry)
    }

    private fun appendToLog(entry: Map<String, Any>) {
        val logs = logConfig.getConfigurationSection("logs") ?: logConfig.createSection("logs")
        val newLogIndex = logs.getKeys(false).size + 1
        val newLogEntry = logs.createSection(newLogIndex.toString())

        entry.forEach { (key, value) ->
            when (value) {
                is String, is Int, is Long, is Double, is Boolean -> newLogEntry.set(key, value)
                else -> newLogEntry.set(key, value.toString()) // Convert to string if not serializable
            }
        }
        saveLogConfig()
    }

    private fun saveLogConfig() {
        try {
            logConfig.save(logFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
