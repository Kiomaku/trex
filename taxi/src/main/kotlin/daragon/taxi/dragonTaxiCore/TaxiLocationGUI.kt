package daragon.taxi.dragonTaxiCore

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class TaxiLocationGUI(private val plugin: DragonTaxiCore) : Listener {

    fun open(player: Player) {
        val inventory: Inventory = Bukkit.createInventory(null, 27, "Select Taxi Destination")

        // Populate the GUI with locations
        val taxiLocations = plugin.getTaxiLocations()
        for (i in taxiLocations.indices) {
            val location = taxiLocations[i]
            val item = ItemStack(Material.MAP)
            val meta: ItemMeta = item.itemMeta!!
            meta.setDisplayName(ChatColor.GREEN.toString() + location.name)
            item.itemMeta = meta
            inventory.setItem(i, item)
        }

        player.openInventory(inventory)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "Select Taxi Destination") {
            event.isCancelled = true
            val player = event.whoClicked as Player
            val clickedItem = event.currentItem

            if (clickedItem != null && clickedItem.type != Material.AIR) {
                val locationName = ChatColor.stripColor(clickedItem.itemMeta!!.displayName)
                val location = plugin.getTaxiLocations().find { it.name == locationName }

                if (location != null) {
                    plugin.createTaxiRequest(player, location)
                    plugin.notifyTaxiMembers(plugin.getMessage("taxi_mg", mapOf("locationname" to location.name, "playername" to player.name)))
                    // Notify player of successful request
                    plugin.notifyPlayer(player, plugin.getMessage("request_success", mapOf("locationName" to location.name)))

                    player.closeInventory()
                }
            }
        }
    }
}
