package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.CurrencyManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PayCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (args.size == 2) {
                val amount = args[1].toIntOrNull()

                if (amount != null && amount > 0) {
                    if (args[0].equals("all", ignoreCase = true)) {
                        handlePayAll(sender, amount)
                    } else {
                        handlePayIndividual(sender, args[0], amount)
                    }
                } else {
                    sender.sendMessage("Invalid amount specified.")
                }
            } else {
                sender.sendMessage("Usage: /gpay <player|all> <amount>")
            }
        } else {
            sender.sendMessage("Only players can use this command.")
        }
        return true
    }

    private fun handlePayAll(sender: Player, amount: Int) {
        val senderBalance = CurrencyManager.getBalance(sender)
        val numberOfPlayers = Bukkit.getOnlinePlayers().size - 1
        if (senderBalance >= amount * numberOfPlayers) {



            CurrencyManager.subtractBalance(sender, sender, amount * numberOfPlayers)

            for (player in Bukkit.getOnlinePlayers()) {
                if (player != sender) {

                    CurrencyManager.addBalance(sender, player, amount)

                    player.sendMessage("You received $amount GCoins from ${sender.name}.")
                }
            }

            sender.sendMessage("You paid $amount GCoins to all players.")
        } else {
            sender.sendMessage("You do not have enough GCoins to pay all players.")
        }
    }

    private fun handlePayIndividual(sender: Player, targetName: String, amount: Int) {
        val target = Bukkit.getPlayer(targetName)
        if (target != null && target != sender) {
            val senderOldBalance = CurrencyManager.getBalance(sender)
            val targetOldBalance = CurrencyManager.getBalance(target)

            if (senderOldBalance >= amount) {
                CurrencyManager.subtractBalance(sender, sender, amount)
                CurrencyManager.addBalance(sender, target, amount)

                val senderNewBalance = CurrencyManager.getBalance(sender)
                val targetNewBalance = CurrencyManager.getBalance(target)

                CurrencyManager.logTransaction(
                    sender, target, senderOldBalance, senderNewBalance, targetOldBalance, targetNewBalance, amount
                )

                sender.sendMessage("You paid $amount GCoins to ${target.name}.")
                target.sendMessage("You received $amount GCoins from ${sender.name}.")
            } else {
                sender.sendMessage("You do not have enough GCoins.")
            }
        } else {
            sender.sendMessage("Player not found or invalid target.")
        }
    }
}
