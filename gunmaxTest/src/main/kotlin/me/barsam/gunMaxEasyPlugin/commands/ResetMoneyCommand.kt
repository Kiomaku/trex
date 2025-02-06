package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.CurrencyManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ResetMoneyCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("gunMaxEasyPlugin.resetmoney")) {
            if (args.size == 1) {
                val target = Bukkit.getPlayer(args[0])

                if (target != null) {
                    CurrencyManager.setBalance(target, 0)
                    sender.sendMessage("Reset ${target.name}'s balance to 0.")
                    target.sendMessage("Your balance has been reset to 0.")
                    return true
                }
            }
            sender.sendMessage("Usage: /resetmoney <player>")
        }
        return true
    }
}
