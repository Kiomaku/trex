package ir.alireza009.koyaGPS.command;

import ir.alireza009.koyaGPS.KoyaGPS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabComplete implements TabCompleter {
    private final List<String> arguments = Arrays.asList("Delete", "Add", "SetIcon", "FastTravel", "FastTravelCost", "Reload", "Give");
    private final List<String> argumentsTwo = Arrays.asList("Disable", "Enable");
    private final List<String> gpsNames = new ArrayList<String>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        gpsNames.clear();
        if (KoyaGPS.getLocationFileManager().getConfig().getConfigurationSection("Locations") != null) {
            gpsNames.addAll(KoyaGPS.getLocationFileManager().getConfig().getConfigurationSection("Locations").getKeys(false));
        }
        if (args.length == 1) {
            return arguments;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("FastTravel")) return argumentsTwo;
            if (args[0].equalsIgnoreCase("Add")) return List.of("<Name>");
            if (args[0].equalsIgnoreCase("SetIcon")) return gpsNames;
            if (args[0].equalsIgnoreCase("Delete")) return gpsNames;
            if (args[0].equalsIgnoreCase("FastTravelCost")) return gpsNames;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("FastTravel")) return gpsNames;
            if (args[0].equalsIgnoreCase("FastTravelCost")) return List.of("<Cost>");
        }

        return null;
    }
}
