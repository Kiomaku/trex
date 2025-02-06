package me.barsam.dragonDiscordCore

import me.barsam.dragonDiscordCore.DragonDiscordCore
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: DragonDiscordCore) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val linked = plugin.config.getString("linked.${player.name}")

        if (linked != null) {
            player.playerListName(Component.text(player.name + " ✓"))
            plugin.logger.info("Player ${player.name} Is Verified!")
        } else {
            player.playerListName(Component.text(player.name + " ✖"))
            plugin.logger.info("Player ${player.name} Is Not Verified!")
        }
    }
}
