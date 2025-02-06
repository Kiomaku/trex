package me.barsam.gunMaxEasyPlugin

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CurrencyManager {
    private lateinit var dataConfig: FileConfiguration
    private lateinit var dataFile: File
    private lateinit var logConfig: FileConfiguration
    private lateinit var logFile: File

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun initialize(plugin: GunMaxEasyPlugin) {

        dataFile = File(plugin.dataFolder, "data.yml")
        if (!dataFile.exists()) {
            dataFile.parentFile.mkdirs()
            dataFile.createNewFile()
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile)


        logFile = File(plugin.dataFolder, "log.yml")
        if (!logFile.exists()) {
            logFile.parentFile.mkdirs()
            logFile.createNewFile()
        }
        logConfig = YamlConfiguration.loadConfiguration(logFile)
    }

    fun reloadConfig() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile)
        logConfig = YamlConfiguration.loadConfiguration(logFile)
    }

    fun getBalance(player: OfflinePlayer): Int {
        return dataConfig.getInt("balances.${player.uniqueId}", 0)
    }

    fun setBalance(player: OfflinePlayer, amount: Int) {
        dataConfig.set("balances.${player.uniqueId}", amount)
        saveDataConfig()
    }

    fun addBalance(sender: CommandSender, player: OfflinePlayer, amount: Int) {
        val oldBalance = getBalance(player)
        val newBalance = oldBalance + amount
        setBalance(player, newBalance)
        if (amount > 0) logPositiveTransaction(sender, player, oldBalance, newBalance, amount)
    }

    fun subtractBalance(sender: CommandSender, player: OfflinePlayer, amount: Int) {
        val oldBalance = getBalance(player)
        val newBalance = oldBalance - amount
        setBalance(player, newBalance)
        if (amount > 0) logNegativeTransaction(sender, player, oldBalance, newBalance, amount)
    }

    fun getTopBalances(limit: Int): List<Map.Entry<UUID, Int>> {
        val balances = mutableMapOf<UUID, Int>()
        for (key in dataConfig.getConfigurationSection("balances")?.getKeys(false) ?: emptySet()) {
            val uuid = UUID.fromString(key)
            val balance = dataConfig.getInt("balances.$key", 0)
            balances[uuid] = balance
        }
        return balances.entries.sortedByDescending { it.value }.take(limit)
    }

    private fun logPositiveTransaction(sender: CommandSender, player: OfflinePlayer, oldBalance: Int, newBalance: Int, amount: Int) {
        logTransaction(sender, player, oldBalance, newBalance, oldBalance, newBalance, amount)
    }

    private fun logNegativeTransaction(sender: CommandSender, player: OfflinePlayer, oldBalance: Int, newBalance: Int, amount: Int) {
        logTransaction(sender, player, oldBalance, newBalance, oldBalance, newBalance, -amount)
    }

    fun logTransaction(
        executor: CommandSender,
        player: OfflinePlayer,
        oldPayerBalance: Int,
        newPayerBalance: Int,
        oldPayeeBalance: Int,
        newPayeeBalance: Int,
        amount: Int
    ) {
        val date = dateFormat.format(Date())
        val logEntry = mapOf(
            "action" to "Payment",
            "date" to date,
            "executor" to (executor.name ?: "Unknown"),
            "player" to (player.name ?: "Unknown"),
            "oldPayerBalance" to oldPayerBalance,
            "newPayerBalance" to newPayerBalance,
            "oldPayeeBalance" to oldPayeeBalance,
            "newPayeeBalance" to newPayeeBalance,
            "amount" to amount
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

    private fun saveDataConfig() {
        try {
            dataConfig.save(dataFile)
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Error saving data.yml file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun saveLogConfig() {
        try {
            logConfig.save(logFile)
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Error saving log.yml file: ${e.message}")
            e.printStackTrace()
        }
    }
}
