package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.CurrencyManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class GiveMoneyCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("gunMaxEasyPlugin.givemoney")) {
            if (args.size == 2) {
                val amount = args[1].toIntOrNull()

                if (amount != null && amount > 0) {
                    if (args[0].equals("all", ignoreCase = true)) {

                        for (player in Bukkit.getOnlinePlayers()) {
                            CurrencyManager.addBalance(sender, player, amount)
                            player.sendMessage("You received $amount GCoins.")
                        }
                        sender.sendMessage("Gave $amount GCoins to all players.")
                        return true
                    } else {

                        val target = Bukkit.getPlayer(args[0])
                        if (target != null) {
                            CurrencyManager.addBalance(sender, target, amount)
                            sender.sendMessage("Gave $amount GCoins to ${target.name}.")
                            target.sendMessage("You received $amount GCoins.")
                            return true
                        } else {
                            sender.sendMessage("Player not found.")
                        }
                    }
                } else {
                    sender.sendMessage("Invalid amount specified.")
                }
            } else {
                sender.sendMessage("Usage: /givemoney <player|all> <amount>")
            }
        }
        return true
    }
}
