package daragon.taxi.dragonTaxiCore

import com.live.bemmamin.gps.api.GPSAPI
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AcceptTaxiCommand(private val plugin: DragonTaxiCore, private val gpsAPI: GPSAPI) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be run by a player.")
            return true
        }

        if (args.isEmpty()) {
            plugin.notifyPlayer(sender, plugin.getMessage("accept_usage"))
            return true
        }

        val playerName = args[0]
        val passenger = Bukkit.getPlayer(playerName)

        if (passenger == null) {
            plugin.notifyPlayer(sender, plugin.getMessage("accept_no_player"))
            return true
        }

        val request = plugin.getTaxiRequest(passenger.uniqueId)

        if (request == null) {
            plugin.notifyPlayer(sender, plugin.getMessage("accept_no_request", mapOf("playerName" to passenger.name)))
            return true
        }

        if (request.isAccepted) {
            plugin.notifyPlayer(sender, plugin.getMessage("accept_already_accepted"))
            return true
        }

        // Mark the request as accepted and assign the driver
        request.isAccepted = true
        request.driverUUID = sender.uniqueId

        // Notify both the driver and the passenger
        plugin.notifyPlayer(sender, plugin.getMessage("accept_taxi_notification", mapOf("playerName" to passenger.name, "driverName" to sender.name)))
        plugin.notifyPlayer(passenger, plugin.getMessage("accept_notification", mapOf("driverName" to sender.name)))

        // Start guiding the driver to the passenger's location
        plugin.guideTaxiDriverToPlayer(sender, passenger)

        return true
    }
}
