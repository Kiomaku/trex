package daragon.taxi.dragonTaxiCore

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CancelTaxiCommand(private val plugin: DragonTaxiCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        val player: Player = sender

        // Cancel the taxi request
        plugin.cancelTaxiRequest(player)
        return true
    }
}
