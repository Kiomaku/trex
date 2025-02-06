package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.CurrencyManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GCoinTopCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val topPlayers = CurrencyManager.getTopBalances(10)

        if (topPlayers.isNotEmpty()) {
            sender.sendMessage("Top GCoin holders:")
            topPlayers.forEachIndexed { index, entry ->
                val playerName = Bukkit.getOfflinePlayer(entry.key).name ?: "Unknown"
                sender.sendMessage("${index + 1}. $playerName - ${entry.value} GCoins")
            }
        } else {
            sender.sendMessage("No GCoin data available.")
        }

        return true
    }
}
