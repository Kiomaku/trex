package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.GunMaxEasyPlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.configuration.file.FileConfiguration

class SetMobMoneyCommand : CommandExecutor {
    private val plugin = GunMaxEasyPlugin.instance

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            sender.sendMessage("Usage: /gsetmobmoney [mob type] [money]")
            return true
        }

        val mobTypeName = args[0].toUpperCase()
        val moneyAmount: Int

        try {
            moneyAmount = args[1].toInt()
        } catch (e: NumberFormatException) {
            sender.sendMessage("Invalid amount of money.")
            return true
        }

        val mobType = EntityType.values().find { it.name == mobTypeName }

        if (mobType == null) {
            sender.sendMessage("Invalid mob type.")
            return true
        }

        val config: FileConfiguration = plugin.config
        val rewards = config.getConfigurationSection("mob-rewards") ?: config.createSection("mob-rewards")
        rewards.set(mobTypeName, moneyAmount)
        plugin.saveConfig()

        sender.sendMessage("Set reward of $moneyAmount to ${mobType.name}.")
        return true
    }
}
