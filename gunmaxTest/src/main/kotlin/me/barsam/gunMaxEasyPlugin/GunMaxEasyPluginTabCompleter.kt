package me.barsam.gunMaxEasyPlugin

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class GunMaxEasyPluginTabCompleter : TabCompleter {

    private val sortedAmounts = listOf("10", "50", "100", "500", "1000")

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        val suggestions = mutableListOf<String>()

        when (command.name.lowercase()) {
            "givemoney" -> {
                if (args.size == 1) {

                    suggestions.addAll(Bukkit.getOnlinePlayers().map { it.name })
                    suggestions.add("all")
                } else if (args.size == 2) {

                    suggestions.addAll(sortedAmounts)
                }
            }
            "takemoney" -> {
                if (args.size == 1) {

                    suggestions.addAll(Bukkit.getOnlinePlayers().map { it.name })
                } else if (args.size == 2) {

                    suggestions.addAll(sortedAmounts)
                }
            }
            "resetmoney" -> {
                if (args.size == 1) {

                    suggestions.addAll(Bukkit.getOnlinePlayers().map { it.name })
                }
            }
            "gpay" -> {
                if (args.size == 1) {

                    suggestions.addAll(Bukkit.getOnlinePlayers().map { it.name })
                    suggestions.add("all")
                } else if (args.size == 2) {
                    if (sender is Player) {

                        val balance = CurrencyManager.getBalance(sender)
                        suggestions.addAll(sortedAmounts)
                        suggestions.add(balance.toString())
                    }
                }
            }
            "gbalance" -> {
                if (args.size == 1) {

                    suggestions.addAll(Bukkit.getOnlinePlayers().map { it.name })
                }
            }
            "gcointop" -> {

                return emptyList()
            }
        }

        return suggestions.filter { it.startsWith(args.last(), ignoreCase = true) }
    }
}
