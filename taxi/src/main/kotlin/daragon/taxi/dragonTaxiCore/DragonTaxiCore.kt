package daragon.taxi.dragonTaxiCore

import com.live.bemmamin.gps.api.GPSAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class DragonTaxiCore : JavaPlugin() {

    private val taxiLocations = mutableListOf<TaxiLocation>()
    private val activeRequests = mutableMapOf<UUID, TaxiRequest>()
    private lateinit var gpsAPI: GPSAPI

    override fun onEnable() {
        logger.info("DragonTaxiCore has been enabled!")

        // Initialize GPS API
        if (Bukkit.getPluginManager().getPlugin("GPS")?.isEnabled == true) {
            gpsAPI = GPSAPI(this)
        } else {
            logger.severe("GPS Plugin not found or not enabled!")
            return
        }

        // Load locations from config
        loadTaxiLocations()

        // Initialize GUI and register commands
        val taxiLocationGUI = TaxiLocationGUI(this)
        getCommand("requesttaxi")?.setExecutor(RequestTaxiCommand(taxiLocationGUI))
        getCommand("accepttaxi")?.setExecutor(AcceptTaxiCommand(this, gpsAPI))
        getCommand("canceltaxi")?.setExecutor(CancelTaxiCommand(this))

        // Register events
        server.pluginManager.registerEvents(taxiLocationGUI, this)

        // Save the default config if it doesn't exist
        saveDefaultConfig()
    }

    override fun onDisable() {
        logger.info("DragonTaxiCore has been disabled!")
    }

    private fun loadTaxiLocations() {
        val config = config
        val locationList = config.getMapList("locations")

        for (locationMap in locationList) {
            val name = locationMap["name"] as String
            val world = locationMap["world"] as String
            val x = (locationMap["x"] as Number).toDouble()
            val y = (locationMap["y"] as Number).toDouble()
            val z = (locationMap["z"] as Number).toDouble()
            val yaw = (locationMap["yaw"] as Number).toFloat()
            val pitch = (locationMap["pitch"] as Number).toFloat()

            val location = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
            taxiLocations.add(TaxiLocation(name, location))
        }
    }

    fun getTaxiLocations(): List<TaxiLocation> = taxiLocations

    fun createTaxiRequest(player: Player, location: TaxiLocation) {
        val request = TaxiRequest(player.uniqueId, location)
        activeRequests[player.uniqueId] = request
        notifyTaxiMembers("${player.name} is requesting a taxi to ${location.name}. Type /accepttaxi ${player.name} to accept the request.", player)
    }

    fun cancelTaxiRequest(player: Player) {
        val request = activeRequests[player.uniqueId]

        if (request != null && request.isAccepted) {
            val driverUUID = request.driverUUID
            if (driverUUID != null) {
                val driver = Bukkit.getPlayer(driverUUID)
                if (driver != null) {
                    notifyPlayer(driver, "${player.name} has canceled the taxi request.")
                }
            }
        }

        if (activeRequests.remove(player.uniqueId) != null) {
            notifyPlayer(player, getMessage("cancel_success"))
        } else {
            notifyPlayer(player, getMessage("cancel_no_request"))
        }
    }

    fun getTaxiRequest(playerUUID: UUID): TaxiRequest? = activeRequests[playerUUID]

    fun removeTaxiRequest(playerUUID: UUID) {
        activeRequests.remove(playerUUID)
    }

    fun notifyTaxiMembers(message: String, excludePlayer: Player? = null) {
        server.onlinePlayers.filter { it.hasPermission("taxi.driver") && it != excludePlayer }.forEach { it.sendMessage(message) }
    }

    fun notifyPlayer(player: Player, message: String) {
        player.sendMessage(message)
    }

    fun guideTaxiDriverToPlayer(driver: Player, passenger: Player) {
        val request = getTaxiRequest(passenger.uniqueId) ?: return

        try {
            gpsAPI.startCompass(driver, passenger.location)
            notifyPlayer(driver, getMessage("guiding_to_player", mapOf("playerName" to passenger.name)))

            // Schedule a task to check if the driver has reached the passenger
            object : BukkitRunnable() {
                var hasArrived = false

                override fun run() {
                    if (!hasArrived) {
                        val distance = driver.location.distance(passenger.location)
                        if (distance < 2) {
                            // Notify both that the driver has arrived
                            notifyPlayer(driver, getMessage("arrived_at_player", mapOf("playerName" to passenger.name)))
                            notifyPlayer(passenger, getMessage("arrived_at_player", mapOf("playerName" to passenger.name)))

                            hasArrived = true

                            // Stop guiding to the passenger and start guiding to the destination
                            gpsAPI.stopGPS(driver)
                            guideTaxiDriverToDestination(driver, passenger)

                            // Cancel the task
                            cancel()
                        }
                    }
                }
            }.runTaskTimer(this, 0L, 20L) // Checks every second

        } catch (e: Exception) {
            // If GPS fails to find a path, notify the driver
            notifyPlayer(driver, "Cannot reach ${passenger.name}. Please try a different route.")
        }
    }

    fun guideTaxiDriverToDestination(driver: Player, passenger: Player) {
        val request = getTaxiRequest(passenger.uniqueId) ?: return

        gpsAPI.startCompass(driver, request.destination.location)

        // Schedule a task to check if the driver has reached the destination
        object : BukkitRunnable() {
            var hasArrived = false

            override fun run() {
                if (!hasArrived) {
                    val distance = driver.location.distance(request.destination.location)
                    if (distance < 2) {
                        // Notify both that the driver has arrived at the destination
                        notifyPlayer(driver, getMessage("arrived_at_destination", mapOf("locationName" to request.destination.name)))
                        notifyPlayer(passenger, getMessage("arrived_at_destination", mapOf("locationName" to request.destination.name)))

                        hasArrived = true

                        // Stop guiding
                        gpsAPI.stopGPS(driver)

                        // Cancel the task
                        cancel()
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L) // Checks every second
    }

    fun getMessage(key: String, placeholders: Map<String, String> = emptyMap()): String {
        var message = config.getString("messages.$key") ?: "Message not found."

        // Replace placeholders
        placeholders.forEach { (placeholder, value) ->
            message = message.replace("{${placeholder}}", value)
        }

        return message
    }
}

data class TaxiLocation(val name: String, val location: Location)
data class TaxiRequest(
    val requesterUUID: UUID,
    val destination: TaxiLocation,
    var isAccepted: Boolean = false,
    var driverUUID: UUID? = null
)
