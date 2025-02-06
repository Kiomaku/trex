package me.barsam.gunMaxEasyPlugin.commands

import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType

class MobTypeTabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: org.bukkit.command.Command, alias: String, args: Array<out String>): List<String>? {
        if (args.size == 1) {
            return EntityType.entries.map { it.name }.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return null
    }
}
