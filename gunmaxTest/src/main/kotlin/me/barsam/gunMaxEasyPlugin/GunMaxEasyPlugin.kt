package me.barsam.gunMaxEasyPlugin

import me.barsam.gunMaxEasyPlugin.commands.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.configuration.file.FileConfiguration

class GunMaxEasyPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: GunMaxEasyPlugin
            private set
    }

    override fun onEnable() {
        instance = this

        // Initialize configuration
        saveDefaultConfig()
        initConfigDefaults()

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            GMoneyPlaceholderExpansion(this).register()
        } else {
            logger.warning("PlaceholderAPI is not installed or not enabled.")
        }
        logger.info("GunMaxEasyPlugin has been enabled!")
        this.getCommand("gbalance")?.setExecutor(BalanceCommand())
        this.getCommand("givemoney")?.setExecutor(GiveMoneyCommand())
        this.getCommand("takemoney")?.setExecutor(TakeMoneyCommand())
        this.getCommand("resetmoney")?.setExecutor(ResetMoneyCommand())
        this.getCommand("gpay")?.setExecutor(PayCommand())
        this.getCommand("gcointop")?.setExecutor(GCoinTopCommand())
        this.getCommand("gsetmobmoney")?.setExecutor(SetMobMoneyCommand())
        this.getCommand("greload")?.setExecutor(ReloadCommand())
        this.getCommand("gsetmobmoney")?.tabCompleter = MobTypeTabCompleter()
        server.pluginManager.registerEvents(MobKillListener(), this)

        val tabCompleter = GunMaxEasyPluginTabCompleter()
        this.getCommand("gbalance")?.tabCompleter = tabCompleter
        this.getCommand("givemoney")?.tabCompleter = tabCompleter
        this.getCommand("takemoney")?.tabCompleter = tabCompleter
        this.getCommand("resetmoney")?.tabCompleter = tabCompleter
        this.getCommand("gpay")?.tabCompleter = tabCompleter
        this.getCommand("gcointop")?.tabCompleter = tabCompleter

        CurrencyManager.initialize(this)
        LogManager.initialize(this)
    }

    override fun onDisable() {
        logger.info("GunMaxEasyPlugin has been disabled!")
    }

    private fun initConfigDefaults() {
        val config: FileConfiguration = this.config
        if (config.getConfigurationSection("mob-rewards") == null) {
            val mobRewards = mapOf(
                "ZOMBIE" to 10,
                "SKELETON" to 15,
                "CREEPER" to 20
            )
            config.createSection("mob-rewards", mobRewards)
            saveConfig()
        }
    }
}
