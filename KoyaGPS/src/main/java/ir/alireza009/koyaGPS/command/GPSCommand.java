package ir.alireza009.koyaGPS.command;

import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.utils.Utils;
import ir.alireza009.koyaGPS.utils.LangUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GPSCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if (!player.hasPermission("gps.admin")) {
            player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_help_title")));
            player.sendMessage(LangUtils.getMessage("gps_help_add"));
            player.sendMessage(LangUtils.getMessage("gps_help_delete"));
            player.sendMessage(LangUtils.getMessage("gps_help_set_icon"));
            player.sendMessage(LangUtils.getMessage("gps_help_fast_travel"));
            player.sendMessage(LangUtils.getMessage("gps_help_fast_travel_cost"));
            player.sendMessage(LangUtils.getMessage("gps_help_give"));
            player.sendMessage(LangUtils.getMessage("gps_help_reload"));
            return false;
        }

        if (args[0].equals("reload")) {
            KoyaGPS.getLocationFileManager().saveDefaultConfig();
            KoyaGPS.getPlayersFileManager().saveDefaultConfig();
            KoyaGPS.getLangFileManager().saveDefaultConfig();
            KoyaGPS.getInstance().saveDefaultConfig();
            player.sendMessage(Utils.colorize(LangUtils.getMessage("plugin_reloaded")));
            return false;
        }

        if (args[0].equalsIgnoreCase("Delete")) {
            if (args.length < 2) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_delete_usage")));
                return false;
            }

            String id = args[1];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id, null);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("location_deleted").replace("{Name}", id)));
            return false;
        }

        if (args[0].equalsIgnoreCase("SetIcon")) {
            if (args.length < 2) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_set_icon_usage")));
                return false;
            }

            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_item_in_hand")));
                return false;
            }

            String icon = String.valueOf(player.getInventory().getItemInMainHand().getType());
            String id = args[1];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Icon", icon);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("icon_changed")));
            return false;
        }

        if (args[0].equalsIgnoreCase("Add")) {
            if (args.length < 2) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_add_usage")));
                return false;
            }

            StringBuilder name = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                name.append(args[i]).append(" ");
            }
            if (name.length() > 0) {
                name.setLength(name.length() - 1);
            }

            String id = name.toString().replace(" ", "-");

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) != null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_exists")));
                return false;
            }

            String icon = "COMPASS";
            Location location = player.getLocation();
            String locationId = location.getWorld().getName() + "@" + location.getBlockX() + "@" + location.getBlockY() + "@" + location.getBlockZ();

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Name", name.toString());
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Icon", icon);
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravel", true);
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravelCost", 0);
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Location", locationId);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("location_added")
                    .replace("{Name}", name.toString())
                    .replace("{X}", String.valueOf(location.getBlockX()))
                    .replace("{Y}", String.valueOf(location.getBlockY()))
                    .replace("{Z}", String.valueOf(location.getBlockZ()))
            ));
            return false;
        }

        if (args[0].equalsIgnoreCase("FastTravel")) {
            if (args.length < 3) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_fast_travel_usage")));
                return false;
            }

            String action = args[1].toLowerCase();
            String id = args[2];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            boolean enable = action.equals("enable");

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravel", enable);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("fast_travel_status")
                    .replace("{Name}", id)
                    .replace("{Status}", enable ? LangUtils.getMessage("enabled") : LangUtils.getMessage("disabled"))
            ));
            return false;
        }

        if (args[0].equalsIgnoreCase("FastTravelCost")) {
            if (KoyaGPS.getEconomy() == null) {
                player.sendMessage(Utils.colorize("Vault and Essentials missing"));
                return false;
            }
            if (args.length < 3) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_fast_travel_cost_usage")));
                return false;
            }

            String id = args[1];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            int cost;
            try {
                cost = Integer.parseInt(args[2]);
            } catch (Exception e) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("invalid_cost")));
                return false;
            }

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravelCost", cost);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("fast_travel_cost_updated")
                    .replace("{Name}", id)
                    .replace("{Cost}", String.valueOf(cost))
            ));
            return false;
        }

        if (args[0].equalsIgnoreCase("Give")) {
            if (args.length < 2) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_give_usage")));
                return false;
            }

            Player target = player.getServer().getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("player_not_found")));
                return false;
            }

            // Get the GPS item settings from the config
            String itemName = KoyaGPS.getInstance().getConfig().getString("GPS.Item.Name", "&8(&b&lGPS&8)");
            String materialName = KoyaGPS.getInstance().getConfig().getString("GPS.Item.Material", "COMPASS");
            Material material = Material.matchMaterial(materialName);

            if (material == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("invalid_material_in_config")));
                return false;
            }

            ItemStack gpsItem = new ItemStack(material);
            ItemMeta meta = gpsItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Utils.colorizeWithoutPrefix(itemName));
                gpsItem.setItemMeta(meta);
            }

            target.getInventory().addItem(gpsItem);
            target.sendMessage(Utils.colorize(LangUtils.getMessage("gps_received")));
            sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_given").replace("{Player}", target.getName())));
            return false;
        }

        return false;
    }
}
