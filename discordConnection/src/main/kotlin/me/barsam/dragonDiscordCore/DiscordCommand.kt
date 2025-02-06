package me.barsam.dragonDiscordCore

import me.barsam.dragonDiscordCore.DragonDiscordCore
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DiscordCommand(private val plugin: DragonDiscordCore) : CommandExecutor {

    // Store code timestamps and player cooldowns
    private val codeTimestamps = ConcurrentHashMap<String, Long>()
    private val cooldowns = ConcurrentHashMap<UUID, Long>()

    private fun generateRandomCode(): String {
        return (1000000 + Random.nextInt(9000000)).toString() // Generates a 7-digit code
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        val player = sender as Player

        when (args.getOrNull(0)) {
            "link" -> {
                if (sender is Player) {
                    val playerName = sender.name
                    val playerUUID = player.uniqueId

                    // Check cooldown
                    val lastUse = cooldowns[playerUUID]
                    if (lastUse != null && (System.currentTimeMillis() - lastUse) < 10 * 60 * 1000) {
                        val remainingTime = ((10 * 60 * 1000) - (System.currentTimeMillis() - lastUse)) / 1000
                        sender.sendMessage("You must wait $remainingTime seconds before using this command again.")
                        return true
                    }

                    // Check if the player is already linked
                    val dataExist = plugin.config.get("linked.$playerName.discordId")
                    if (dataExist != null) {
                        sender.sendMessage("You have already linked your account.")
                        return true
                    }

                    val randomCode = generateRandomCode()

                    // Save the mapping in the configuration
                    plugin.config.set("$randomCode.username", playerName)
                    plugin.config.set("$randomCode.discordId", "NoDiscordLinked") // Initial state
                    plugin.saveConfig()

                    // Set timestamp for code expiration (2 minutes)
                    codeTimestamps[randomCode] = System.currentTimeMillis()

                    // Set cooldown (10 minutes)
                    cooldowns[playerUUID] = System.currentTimeMillis()

                    // Inform the player
                    sender.sendMessage("Your linking code is: **$randomCode**. Please send this code to the Discord bot to link your account. This code will expire in 2 minutes.")

                    // Schedule task to remove the code after 2 minutes
                    object : BukkitRunnable() {
                        override fun run() {
                            if (codeTimestamps.containsKey(randomCode) && (System.currentTimeMillis() - codeTimestamps[randomCode]!!) >= 2 * 60 * 1000) {
                                plugin.config.set("$randomCode", null)
                                plugin.saveConfig()
                                codeTimestamps.remove(randomCode)
                            }
                        }
                    }.runTaskLater(plugin, 2 * 60 * 20L) // 2 minutes in ticks
                }
                return true
            }
            "unlink" -> {
                if (sender.hasPermission("dragondiscordcore.unlink")) { // Check if sender has permission
                    if (args.size != 2) {
                        sender.sendMessage("Usage: /discord unlink [username]")
                        return true
                    }

                    val targetUsername = args[1]
                    val suffix = " &c✖"  // The suffix you want to set, for example, a check mark
                    val command = "lp user ${player.name} meta setsuffix \"$suffix\""

                    // Run the command on the main thread to avoid asynchronous errors

                    val dataExist = plugin.config.get("linked.$targetUsername.discordId")
                    if (dataExist != null) {
                        plugin.removeDiscordRole(dataExist.toString())
                        Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, command)

                        plugin.config.set("linked.$targetUsername", null)  // Save under player's name
                        plugin.saveConfig()

                        sender.sendMessage("Successfully unlinked $targetUsername from Discord.")
                    } else {
                        sender.sendMessage("No linked account found for $targetUsername.")
                    }

                } else {
                    sender.sendMessage("You do not have permission to perform this action.")
                }
                return true
            }
        }

        return true
    }
}
