package net.mcplayhd.ping;

import net.mcplayhd.ping.commands.CMDping;
import net.mcplayhd.ping.listeners.PlayerListener;
import net.mcplayhd.ping.utils.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public class Ping extends JavaPlugin {

    public static Ping instance;
    public static Updater updater;

    public static String PREFIX;
    public static String MSG_YOURPING;
    public static String MSG_OTHERPING;
    public static String MSG_ALLPINGS;
    public static String MSG_NOPERMISSIONS;
    public static String MSG_NOTONLINE;
    public static boolean TABPING;
    public static int TABDELAY;

    public static BukkitTask TABTASK;

    public static boolean UPDATEAVAILABLE;

    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        getCommand("ping").setExecutor(new CMDping());
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    public static void loadConfig() {
        instance.reloadConfig();
        if (TABTASK != null && TABTASK.isSync()) TABTASK.cancel();
        boolean SEARCHFORUPDATE = instance.getConfig().getBoolean("search-for-update");
        boolean AUTOUPDATE = instance.getConfig().getBoolean("auto-update");
        TABPING = instance.getConfig().getBoolean("tabPing");
        TABDELAY = instance.getConfig().getInt("tabPingTime");
        PREFIX = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("prefix"));
        MSG_YOURPING = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("yourPing")).replace("%prefix", PREFIX);
        MSG_OTHERPING = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("otherPing")).replace("%prefix", PREFIX);
        MSG_ALLPINGS = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("allPings")).replace("%prefix", PREFIX);
        MSG_NOPERMISSIONS = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("noPermissions")).replace("%prefix", PREFIX);
        MSG_NOTONLINE = ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("notOnline")).replace("%prefix", PREFIX);
        if (SEARCHFORUPDATE) {
            updater = new Updater(instance, 88948, instance.getFile(), (AUTOUPDATE ? Updater.UpdateType.DEFAULT : Updater.UpdateType.NO_DOWNLOAD), true);
            if (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                instance.getLogger().info("New version available! " + updater.getLatestName());
                UPDATEAVAILABLE = !AUTOUPDATE;
            }
        }
        if (!TABPING) return;
        tabPing();
    }

    private static String getVersion(Server server) {
        final String packageName = server.getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static int getPing(Player p) {
        String version = getVersion(instance.getServer());
        if (version.startsWith("v1_8")) {
            return ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer)p).getHandle().playerConnection.player.ping;
        } else if (version.startsWith("v1_9")) {
            return ((org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer)p).getHandle().playerConnection.player.ping;
        } else if (version.startsWith("v1_10")) {
            return ((org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer)p).getHandle().playerConnection.player.ping;
        } else if (version.startsWith("v1_11")) {
            return ((org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer)p).getHandle().playerConnection.player.ping;
        } else if (version.startsWith("v1_12")) {
            return ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer)p).getHandle().playerConnection.player.ping;
        } else if (version.startsWith("v1_13")) {
            return ((org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer)p).getHandle().playerConnection.player.ping;
        } else {
            return ((CraftPlayer)p).getHandle().playerConnection.player.ping;
        }
    }

    public static void tabPing() {
        TABTASK = new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<String, Integer> pings = new HashMap<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    pings.put(p.getName(), getPing(p));
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Scoreboard sb = p.getScoreboard();
                    if (sb == null) {
                        sb = Bukkit.getScoreboardManager().getNewScoreboard();
                        p.setScoreboard(sb);
                    }
                    Objective ob = sb.getObjective("PingTab");
                    if (ob == null) {
                        sb.registerNewObjective("PingTab", "dummy").setDisplaySlot(DisplaySlot.PLAYER_LIST);
                        ob = sb.getObjective("PingTab");
                    }
                    for (Map.Entry<String, Integer> e : pings.entrySet()) {
                        ob.getScore(e.getKey()).setScore(e.getValue());
                    }
                    p.setScoreboard(sb);
                }
            }
        }.runTaskTimer(instance, 0, TABDELAY);
    }

}
