package me.barsam.dragonDiscordCore

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import net.dv8tion.jda.api.requests.GatewayIntent
import org.bukkit.scheduler.BukkitRunnable

class DragonDiscordCore : JavaPlugin() {

    private lateinit var jda: JDA

    override fun onEnable() {
        saveDefaultConfig()

        // Load bot token and guild ID from config
        val token = config.getString("bot-token") ?: return
        val guildId = config.getString("guild-id") ?: return

        // Initialize JDA
        jda = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Enable MESSAGE_CONTENT intent
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .build()

        // Register Discord command and button handler
        jda.addEventListener(DiscordBotListener(this))
        startUpdatingPlayerCount()
        // Register commands and event listeners
        getCommand("discordlink")?.setExecutor(DiscordCommand(this))
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
    }
    private fun startUpdatingPlayerCount() {
        object : BukkitRunnable() {
            override fun run() {
                // Get the player count
                val onlinePlayers = Bukkit.getOnlinePlayers().size

                // Update the bot's activity to show the number of online players
                jda.presence.activity = Activity.watching("$onlinePlayers Minecraft RolePlayers")
            }
        }.runTaskTimer(this, 0L, 60L) // Update every 3 seconds (60 ticks = 3 seconds)
    }
    fun removeDiscordRole(discordId: String) {
        val guild = config.getString("guild-id")?.let { jda.getGuildById(it) } ?: return
        val verifyRoleId = config.getString("verify-role-id") ?: return

        val member = guild.getMemberById(discordId)
        if (member != null) {
            val role = guild.getRoleById(verifyRoleId)
            if (role != null) {
                guild.removeRoleFromMember(member, role).queue {
                    Bukkit.getLogger().info("Removed role from user: $discordId")
                }
            } else {
                Bukkit.getLogger().warning("Verify role not found for ID: $verifyRoleId")
            }
        } else {
            Bukkit.getLogger().warning("Member not found for ID: $discordId")
        }
    }

    override fun onDisable() {
        // Shutdown Discord bot when plugin is disabled
        if (::jda.isInitialized) {
            jda.shutdown()
        }
    }
    // Function to get the Minecraft username based on Discord ID
// Function to get Minecraft username from Discord ID
    fun getMinecraftUsernameByDiscordId(discordId: String): String? {
        val linkedPlayers = config.getConfigurationSection("linked")
        linkedPlayers?.getKeys(false)?.forEach { username ->
            val linkedDiscordId = config.getString("linked.$username.discordId")
            if (linkedDiscordId == discordId) {
                return username
            }
        }
        return null
    }






    fun syncAccount(playerUuid: String, discordId: String) {
        val guildId = config.getString("guild-id") ?: return
        val verifyRoleId = config.getString("verify-role-id") ?: return
        val player = Bukkit.getPlayer(playerUuid) ?: return
        val suffix = " &a✓"  // The suffix you want to set, for example, a check mark
        val command = "lp user ${player.name} meta setsuffix \"$suffix\""
        val command1 = "lp user ${player.name} meta clear"

        // Run the command on the main thread to avoid asynchronous errors
        Bukkit.getScheduler().runTask(this, Runnable {
            Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, command1)
            Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, command)
        })

        logger.info("Player ${player.name} Is Verified!")
        logger.info("Successfully added verification tick to ${player.name}")

        // Get the guild from JDA
        val guild = jda.getGuildById(guildId)
        if (guild == null) {
            logger.warning("Guild not found for ID: $guildId")
            return
        }

        // Fetch the member by Discord ID, even if they are offline
        guild.retrieveMemberById(discordId).queue({ member ->
            if (member == null) {
                logger.warning("Discord member not found for ID: $discordId")
                return@queue
            }

            // Log member details for debugging
            logger.info("Member ID: ${member.id}, Name: ${member.effectiveName}")

            // Add verify role in Discord
            val role = guild.getRoleById(verifyRoleId)
            if (role != null) {
                guild.addRoleToMember(member, role).queue(
                    {
                        logger.info("Successfully added role to member: ${member.effectiveName}")
                    },
                    { error ->
                        logger.warning("Failed to add role: ${error.message}")
                    }
                )
            }

            // Change the Discord nickname to the player's username
            val newNickname = player.name
            guild.modifyNickname(member, newNickname).queue(
                {
                    logger.info("Successfully changed nickname for ${member.effectiveName} to $newNickname")
                },
                { error ->
                    logger.warning("Failed to change nickname: ${error.message}")
                }
            )
        }, { error ->
            logger.warning("Error retrieving Discord member: ${error.message}")
        })
    }

}
