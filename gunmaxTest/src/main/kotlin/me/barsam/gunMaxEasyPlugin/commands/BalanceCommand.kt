package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.CurrencyManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BalanceCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val balance = CurrencyManager.getBalance(sender)
            sender.sendMessage("Your balance is $balance coins.")
            return true
        }
        return false
    }
}
