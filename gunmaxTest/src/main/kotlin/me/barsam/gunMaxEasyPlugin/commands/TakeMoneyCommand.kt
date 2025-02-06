package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.CurrencyManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class TakeMoneyCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("gunMaxEasyPlugin.takemoney")) {
            if (args.size == 2) {
                val target = Bukkit.getPlayer(args[0])
                val amount = args[1].toIntOrNull()

                if (target != null && amount != null && amount > 0) {
                    CurrencyManager.subtractBalance(sender, target, amount)
                    sender.sendMessage("Took $amount coins from ${target.name}.")
                    target.sendMessage("$amount coins have been taken from your balance.")
                    return true
                }
            }
            sender.sendMessage("Usage: /takemoney <player> <amount>")
        }
        return true
    }
}
