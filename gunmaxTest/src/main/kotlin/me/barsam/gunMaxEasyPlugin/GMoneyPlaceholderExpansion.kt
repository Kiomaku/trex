package me.barsam.gunMaxEasyPlugin

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class GMoneyPlaceholderExpansion(private val plugin: JavaPlugin) : PlaceholderExpansion() {

    override fun getAuthor(): String {
        return "barsam"
    }

    override fun getIdentifier(): String {
        return "gmoney"
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun onPlaceholderRequest(player: Player, identifier: String): String? {
        return when {
            identifier.startsWith("balancetop_") -> {
                val rank = identifier.substring("balancetop_".length).toIntOrNull()
                getTopBalanceForRank(rank)
            }

            identifier.equals("balance", ignoreCase = true) -> getBalance(player)
            identifier.startsWith("balance_") -> {
                val targetName = identifier.substring("balance_".length)
                getBalance(Bukkit.getOfflinePlayer(targetName).player)
            }

            else -> null
        }
    }

    private fun getBalance(player: Player?): String {
        if (player == null) return "Player not found"
        val balance = CurrencyManager.getBalance(player)
        return balance.toString()
    }

    private fun getTopBalanceForRank(rank: Int?): String {
        if (rank == null || rank < 1) return "Invalid rank"
        val topBalances = CurrencyManager.getTopBalances(10)
        return if (rank > topBalances.size) {
            "Rank $rank not available"
        } else {
            val entry = topBalances[rank - 1]
            val playerName = Bukkit.getOfflinePlayer(entry.key).name ?: "Unknown"
            "${rank}. $playerName: ${entry.value} GCoins"
        }
    }


}
