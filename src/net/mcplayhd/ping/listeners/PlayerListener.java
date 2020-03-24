package net.mcplayhd.ping.listeners;

import net.mcplayhd.ping.Ping;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static net.mcplayhd.ping.Ping.PREFIX;
import static net.mcplayhd.ping.Ping.UPDATEAVAILABLE;

public class PlayerListener implements Listener {

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (UPDATEAVAILABLE && p.isOp()) {
            p.sendMessage(PREFIX + "§bA new version is available: §e" + Ping.updater.getLatestName());
            p.sendMessage(PREFIX + "§bGet it from: " + Ping.updater.getLatestFileLink());
        }
    }

}
