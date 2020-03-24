package net.mcplayhd.ping.commands;

import net.mcplayhd.ping.Ping;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.mcplayhd.ping.Ping.*;

public class CMDping implements CommandExecutor {

    private void help(Player p) {
        p.sendMessage("§7-= §eConnectionPing §eby McPlayHD §7=-");
        p.sendMessage("§8- §e/ping §7§oYour ping");
        if (p.hasPermission("ping.other")) {
            p.sendMessage("§8- §e/ping <name> §7§oPing of <name>");
        }
        if (p.hasPermission("ping.all")) {
            p.sendMessage("§8- §e/ping all §7§oPing of all online players");
        }
        if (p.hasPermission("ping.mod")) {
            p.sendMessage("§8- §e/ping reload §7§oReloads the config");
            p.sendMessage("§8- §e/ping tab §7§oToggles TAB-ping");
        }
        p.sendMessage("§7-==-");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You have to be a player");
            return true;
        }
        Player p = (Player) sender;
        switch (args.length) {
            case 0:
                p.sendMessage(MSG_YOURPING.replace("%ping", "" + Ping.getPing(p)).replace("%player", p.getDisplayName()));
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "help":
                        help(p);
                    case "reload":
                        if (!p.hasPermission("ping.mod")) {
                            p.sendMessage(MSG_NOPERMISSIONS);
                            return true;
                        }
                        Ping.loadConfig();
                        p.sendMessage("§aConfig reloaded");
                        break;
                    case "tab":
                        if (!p.hasPermission("ping.mod")) {
                            p.sendMessage(MSG_NOPERMISSIONS);
                            return true;
                        }
                        if (TABPING) {
                            if (TABTASK != null && TABTASK.isSync()) {
                                TABTASK.cancel();
                            }
                            instance.getConfig().set("tabPing", false);
                            TABPING = false;
                            for (Player alle : Bukkit.getOnlinePlayers()) {
                                if (alle.getScoreboard().getObjective("PingTab") == null) continue;
                                alle.getScoreboard().getObjective("PingTab").unregister();
                            }
                            p.sendMessage(PREFIX + "§eTabPing is now §cdisabled");
                        } else {
                            if (TABTASK != null && TABTASK.isSync()) {
                                TABTASK.cancel();
                            }
                            Ping.tabPing();
                            instance.getConfig().set("tabPing", true);
                            TABPING = true;
                            p.sendMessage(PREFIX + "§eTabPing is now §aenabled");
                        }
                        break;
                    case "all":
                        if (!p.hasPermission("ping.all")) {
                            p.sendMessage(MSG_NOPERMISSIONS);
                            return true;
                        }
                        p.sendMessage("§7 ");
                        p.sendMessage(MSG_ALLPINGS);
                        for (Player alle : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(MSG_OTHERPING.replace("%ping", "" + Ping.getPing(alle)).replace("%player", alle.getDisplayName()));
                        }
                        break;
                    default:
                        if (!p.hasPermission("ping.other")) {
                            p.sendMessage(MSG_NOPERMISSIONS);
                            return true;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            p.sendMessage(MSG_NOTONLINE);
                            return true;
                        }
                        p.sendMessage(MSG_OTHERPING.replace("%ping", "" + Ping.getPing(target)).replace("%player", target.getDisplayName()));
                        break;
                }
                break;
            default:
                help(p);
                break;
        }
        return true;
    }

}
