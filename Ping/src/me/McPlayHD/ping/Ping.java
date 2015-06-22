package me.McPlayHD.ping;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Ping extends JavaPlugin implements Listener {
	
	protected Logger log;
	protected UpdateChecker updateChecker;
	
	BukkitTask run = null;
	
	private String prefix = "§7[§aPing§7] ";
	
	public void onEnable() {
		log = getLogger();
		getConfig().options().copyDefaults(true);
		saveConfig();
		tabPing();
		this.updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/bukkit-plugins/connection-ping/files.rss");
		getServer().getPluginManager().registerEvents(this, this);
		if(updateChecker.updateNeeded()) {
			log.info("A new version is available: " + this.updateChecker.getVersion());
			log.info("Get it from: " + this.updateChecker.getLink());
		}
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("ping")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length == 2 && p.hasPermission("ping.mod") && args[0].equalsIgnoreCase("tab")) {
					switch(args[1]) {
					case "true":
						getConfig().set("tabPing", true);
						saveConfig();
						reloadConfig();
						tabPing();
						p.sendMessage(prefix + "§3TabPing is now enabled.");
						break;
					case "false":
						getConfig().set("tabPing", false);
						saveConfig();
						reloadConfig();
						run.cancel();
						for(Player alle : Bukkit.getOnlinePlayers()) {
							alle.getScoreboard().getObjective("PingTab").unregister();
						}
						p.sendMessage(prefix + "§3TabPing is now disabled.");
						break;
					default:
						p.sendMessage("§cWrong usage... §b/ping tab (true/false)");
						break;
					}
					return true;
				}
				if(args.length == 1) {
					if(p.hasPermission("ping.mod") && args[0].equalsIgnoreCase("reload")) {
						reloadConfig();
						run.cancel();
						tabPing();
						p.sendMessage(prefix + "§aReload completed");
						return true;
					}
					if(p.hasPermission("ping.mod") && args[0].equalsIgnoreCase("tab")) {
						if(getConfig().getBoolean("tabPing")) {
							getConfig().set("tabPing", false);
							saveConfig();
							reloadConfig();
							run.cancel();
							for(Player alle : Bukkit.getOnlinePlayers()) {
								alle.getScoreboard().getObjective("PingTab").unregister();
							}
							p.sendMessage(prefix + "§3TabPing is now disabled.");
						} else {
							getConfig().set("tabPing", true);
							saveConfig();
							reloadConfig();
							tabPing();
							p.sendMessage(prefix + "§3TabPing is now enabled.");
						}
						return true;
					}
					if(p.hasPermission("ping.all") && args[0].equalsIgnoreCase("all")) {
						String message = "§7-= §a" + ChatColor.translateAlternateColorCodes('&', getConfig().getString("allPings")) + " §7=-";
						for(Player alle : Bukkit.getOnlinePlayers()) {
							int ping = Utils.getPlayerPing(alle);
							message = message + "\n§a" + alle.getDisplayName() + "§a's Ping: §e" + ping + " ms";
						}
						p.sendMessage(message);
						return true;
					}
					if(p.hasPermission("ping.other") && Bukkit.getPlayer(args[0]) != null) {
						int ping = Utils.getPlayerPing(Bukkit.getPlayer(args[0]));
						p.sendMessage("§a" + Bukkit.getPlayer(args[0]).getDisplayName() + "§a's Ping: §e" + ping + " ms");
						return true;
					}
				}
				int ping = Utils.getPlayerPing(Bukkit.getPlayer(p.getName()));
				p.sendMessage("§a" + ChatColor.translateAlternateColorCodes('&', getConfig().getString("yourPing")) + " §e" + ping + " ms");
				return true;
			}
			sender.sendMessage("You have to be a player");
			return true;
		}
		return false;
	}
	
	public void tabPing() {
		int delay = 3;
		try {
			delay = Integer.parseInt(getConfig().getString("tabPingTime"));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		delay = delay * 20;
		run = new BukkitRunnable() {
			@Override
			public void run() {
				if(getConfig().getBoolean("tabPing")) {
					if(Bukkit.getOnlinePlayers().length > 0) {
						for(Player p : Bukkit.getOnlinePlayers()) {
							Scoreboard sb = p.getScoreboard();
							if(sb == null) {
								sb = Bukkit.getScoreboardManager().getNewScoreboard();
							}
							Objective ob = sb.getObjective("PingTab");
							if(ob == null) {
								sb.registerNewObjective("PingTab", "dummy").setDisplaySlot(DisplaySlot.PLAYER_LIST);
								ob = sb.getObjective("PingTab");
							}
							for(Player alle : Bukkit.getOnlinePlayers()) {
								int ping = Utils.getPlayerPing(alle);
								ob.getScore(alle.getDisplayName()).setScore(ping);
							}
							p.setScoreboard(sb);
						}
					}
				} else {
					cancel();
				}
			}
		}.runTaskTimer(this, 0, delay);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(updateChecker.updateNeeded() && p.isOp()) {
			p.sendMessage(prefix + "§bA new version is available: §e" + this.updateChecker.getVersion());
			p.sendMessage(prefix + "§bGet it from: §e" + this.updateChecker.getLink());
		}
	}

}
