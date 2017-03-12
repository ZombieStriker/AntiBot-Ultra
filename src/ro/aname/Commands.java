package ro.aname;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ro.aname.utils.WhitelistManager;

/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Commands implements CommandExecutor {

    private ConfigHandler configHandler = new ConfigHandler();
    private WhitelistManager whitelistManager = new WhitelistManager();

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if ((sender.hasPermission("abu.help") || sender.hasPermission("abu.reload") || sender.hasPermission("abu.add") || sender.hasPermission("abu.remove")) && args.length == 0) {
            sender.sendMessage("[AntiBot-Ultra] -> Available commands: help, reload, add, remove");
        }
        if (cmd.getName().equalsIgnoreCase("abu") && args.length == 1) {
            final String s;
            switch (s = args[0]) {
                case "reload": {
                    if (sender.hasPermission("abu.reload")) {
                        sender.sendMessage("[AntiBot-Ultra] Usage -> /abu reload");
                    }
                    break;
                }
                case "add": {
                    if (sender.hasPermission("abu.add")) {
                        sender.sendMessage("[AntiBot-Ultra] Usage -> /abu add <player>");
                    }
                    break;
                }
                case "remove": {
                    if (sender.hasPermission("abu.remove")) {
                        sender.sendMessage("[AntiBot-Ultra] Usage -> /abu remove <player>");
                    }
                    break;
                }
                case "help": {
                    if (sender.hasPermission("abu.help") || sender.hasPermission("abu.reload") || sender.hasPermission("abu.add") || sender.hasPermission("abu.remove")) {
                        sender.sendMessage("[AntiBot-Ultra] -> Available commands:");
                        sender.sendMessage("/abu reload -> Reloads the config!");
                        sender.sendMessage("/abu add <player> -> Adds the <player> to the whitelist!");
                        sender.sendMessage("/abu remove <player> -> Removes the <player> from the whitelist!");
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
        if (sender.hasPermission("abu.add") || sender.hasPermission("abu.remove") || sender.hasPermission("abu.reload")) {
            if (cmd.getName().equalsIgnoreCase("abu") && args.length == 2) {
                final String s2;
                Player target = Bukkit.getServer().getPlayer(args[1]);
                switch (s2 = args[0]) {
                    case "add": {
                        if (whitelistManager.isOnWhitelist(target)) {
                            sender.sendMessage("[AntiBot-Ultra] -> Player " + target.getName() + " is already on the whitelist!");
                            break;
                        }
                        if (!target.isOnline()) {
                            sender.sendMessage("[AntiBot-Ultra] -> As a measure of protection, player must be online to be added to the whitelist! (Will removed in future updates)");
                            break;
                        }
                        whitelistManager.setWhitelist(target);
                        if (configHandler.getCustomConfig().getLong("Whitelist.add-delay") == 1) {
                            sender.sendMessage("[AntiBot-Ultra] -> Player " + target.getName() + " will be added to the whitelist in " + configHandler.getCustomConfig().getLong("Whitelist.add-delay") + " minute!");
                            break;
                        }
                        sender.sendMessage("[AntiBot-Ultra] -> Player " + target.getName() + " will be added to the whitelist in " + configHandler.getCustomConfig().getLong("Whitelist.add-delay") + " minutes!");
                        break;
                    }
                    case "remove": {
                        if (target.isOp()) {
                            sender.sendMessage("[AntiBot-Ultra] -> Cannot remove " + target.getName() + " from the whitelist because he is an operator!");
                            break;
                        }
                        if (!whitelistManager.isOnWhitelist(target)) {
                            sender.sendMessage("[AntiBot-Ultra] -> Cannot remove player from the whitelist because he is not on the whitelist!");
                            break;
                        }
                        whitelistManager.removeWhitelist(target);
                        sender.sendMessage("[AntiBot-Ultra] -> Player " + target.getName() + " was removed from the whitelist!");
                        break;
                    }
                    case "reload": {
                        configHandler.saveCustomConfig();
                        configHandler.reloadCustomConfig();
                        sender.sendMessage("[AntiBot-Ultra] -> Config reloaded!");
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return false;
    }

}
