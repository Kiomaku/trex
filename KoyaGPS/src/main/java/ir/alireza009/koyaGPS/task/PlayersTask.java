package ir.alireza009.koyaGPS.task;

import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.storage.Storage;
import ir.alireza009.koyaGPS.utils.LangUtils;
import ir.alireza009.koyaGPS.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayersTask {
    private static final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static final Map<UUID, ArmorStand> arrowStands = new HashMap<>();

    public static void startDestination(Player player) {
        BossBar bossBar = KoyaGPS.getInstance().getServer().createBossBar(
                Utils.colorizeWithoutPrefix("&b&lGPS"),
                BarColor.BLUE,
                BarStyle.SEGMENTED_20);

        bossBar.addPlayer(player);
        bossBars.put(player.getUniqueId(), bossBar);

        Location destination = Storage.getPlayers().get(player.getUniqueId());

        // Create and spawn the arrow ArmorStand
        ArmorStand arrowStand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        arrowStand.setVisible(false);
        arrowStand.setGravity(false);
        arrowStand.setSmall(true);
        arrowStand.setMarker(true);
        arrowStand.setHelmet(new ItemStack(Material.ARROW)); // Use a regular arrow item
        arrowStands.put(player.getUniqueId(), arrowStand);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || Storage.getPlayers().get(player.getUniqueId()) == null) {
                    cleanup(player);
                    cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                int distance = (int) playerLoc.distance(destination);

                if (distance < 6) {
                    cleanup(player);
                    cancel();
                    return;
                }

                updateBossBar(player, bossBar, distance);
                updateActionBar(player, distance);
                updateArrow(player, arrowStand, destination);
            }
        }.runTaskTimer(KoyaGPS.getInstance(), 0, 5);
    }

    private static void updateBossBar(Player player, BossBar bossBar, int distance) {
        if (KoyaGPS.getInstance().getConfig().getBoolean("Bossbar")) {
            String title = LangUtils.getMessage("bossbar_title").replace("{Distance}", String.valueOf(distance));
            bossBar.setTitle(Utils.colorizeWithoutPrefix(title));
            bossBar.setProgress(Math.max(0.0, Math.min(1.0, 1.0 - (distance / 100.0))));
        }
    }

    private static void updateActionBar(Player player, int distance) {
        if (KoyaGPS.getInstance().getConfig().getBoolean("Actionbar")) {
            String actionbar = LangUtils.getMessage("actionbar").replace("{Distance}", String.valueOf(distance));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.colorizeWithoutPrefix(actionbar)));
        }
    }

    private static void updateArrow(Player player, ArmorStand arrowStand, Location destination) {
        Location playerLoc = player.getLocation();
        Vector direction = destination.toVector().subtract(playerLoc.toVector()).normalize();

        // Set the arrow's location to be slightly above and in front of the player
        Location arrowLoc = playerLoc.clone().add(direction.clone().multiply(2)).add(0, 1.5, 0);
        arrowStand.teleport(arrowLoc);

        // Calculate the rotation for the arrow to point towards the destination
        double dx = destination.getX() - arrowLoc.getX();
        double dz = destination.getZ() - arrowLoc.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.asin((destination.getY() - arrowLoc.getY()) / destination.distance(arrowLoc)));

        // Apply the rotation to the ArmorStand's head
        arrowStand.setRotation(yaw, pitch);
        arrowStand.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, 0));
    }

    private static void cleanup(Player player) {
        UUID playerId = player.getUniqueId();
        Storage.getPlayers().remove(playerId);

        BossBar bossBar = bossBars.remove(playerId);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }

        ArmorStand arrowStand = arrowStands.remove(playerId);
        if (arrowStand != null) {
            arrowStand.remove();
        }
    }

    public static void stopAllBossBars() {
        for (UUID playerId : bossBars.keySet()) {
            BossBar bossBar = bossBars.get(playerId);
            if (bossBar != null) {
                bossBar.removeAll();
            }
        }
        bossBars.clear();

        for (ArmorStand arrowStand : arrowStands.values()) {
            arrowStand.remove();
        }
        arrowStands.clear();

        Storage.getPlayers().clear();
    }
}

