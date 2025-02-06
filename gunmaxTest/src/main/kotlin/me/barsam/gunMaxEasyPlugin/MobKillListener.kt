package me.barsam.gunMaxEasyPlugin

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.entity.EntityType
import org.bukkit.configuration.file.FileConfiguration

class MobKillListener : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val killer = entity.killer ?: return

        val mobType = entity.type
        val reward = getRewardForMob(mobType)

        if (reward > 0) {
            CurrencyManager.addBalance(killer, killer, reward)
            killer.sendMessage("You received $reward GCoins for killing a ${mobType.name}.")
            LogManager.logMobKill(killer.name, mobType.name, reward)
        }
    }

    private fun getRewardForMob(mobType: EntityType): Int {
        val config: FileConfiguration = GunMaxEasyPlugin.instance.config
        val rewards = config.getConfigurationSection("mob-rewards") ?: return 0
        return rewards.getInt(mobType.name, 0)
    }
}
