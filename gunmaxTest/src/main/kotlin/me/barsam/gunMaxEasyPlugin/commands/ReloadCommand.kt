package me.barsam.gunMaxEasyPlugin.commands

import me.barsam.gunMaxEasyPlugin.CurrencyManager
import me.barsam.gunMaxEasyPlugin.GunMaxEasyPlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadCommand : CommandExecutor {
    private val plugin = GunMaxEasyPlugin.instance

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        plugin.reloadConfig()  // Reload the config.yml
        CurrencyManager.reloadConfig()  // Reload the data and log configurations
        sender.sendMessage("Configuration files have been reloaded.")
        return true
    }
}
