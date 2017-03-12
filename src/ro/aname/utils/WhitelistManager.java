package ro.aname.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ro.aname.AntiBotUltra;
import ro.aname.ConfigHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.IntStream;

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

public class WhitelistManager {

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    private BukkitTask bukkitTask = null;

    private boolean whitelistActive = false;

    private ConfigHandler configHandler = new ConfigHandler();

    private List<String> playersInConfig = this.getCustomConfig().getStringList("Whitelisted-Players");

    public void reloadCustomConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(AntiBotUltra.getInstance().getDataFolder(), "whitelist.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        // Look for defaults in the jar
        Reader defConfigStream = new InputStreamReader(AntiBotUltra.getInstance().getResource("whitelist.yml"));
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }

    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            System.out.println("[AntiBot-Ultra] -> Could not save config for " + customConfigFile);
            ex.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(AntiBotUltra.getInstance().getDataFolder(), "whitelist.yml");
        }
        if (!customConfigFile.exists()) {
            AntiBotUltra.getInstance().saveResource("whitelist.yml", false);
        }
    }

    public boolean isOnWhitelist(Player player) {
        if (configHandler.getCustomConfig().getBoolean("Protection.enabled") || configHandler.getCustomConfig().getBoolean("Whitelist.add-enabled")) {
            List<String> stringList = this.getCustomConfig().getStringList("Whitelisted-Players");
            if (IntStream.range(0, stringList.size()).mapToObj(stringList::get).anyMatch(playersInConfig -> playersInConfig.equalsIgnoreCase(player.getName()))) {
                System.out.println("[AntiBot-Ultra] -> Player " + player.getName() + " was found on the whitelist!");
                return true;
            }
        }
        return false;
    }

    public boolean setWhitelist(Player player) {
        if (configHandler.getCustomConfig().getBoolean("Protection.enabled") || configHandler.getCustomConfig().getBoolean("Whitelist.add-enabled")) {
            bukkitTask = Bukkit.getScheduler().runTaskLater(AntiBotUltra.getInstance(), () -> {
                if (isOnWhitelist(player)) {
                    bukkitTask.cancel();
                }
                if (this.getWhitelistActive()) {
                    bukkitTask.cancel();
                }
                if (!player.isOnline()) {
                    // Even if it doesn't get added to the list, just to be sure :)
                    if (playersInConfig.contains(player.getName())) playersInConfig.remove(player.getName());
                    System.out.println("[AntiBot-Ultra] -> whitelist task (" + bukkitTask.getTaskId() + ") for player " + player.getName() + " has been cancelled! Reason: Player left the server!");
                    Bukkit.getOnlinePlayers().stream().filter(playerMsg -> player.hasPermission("abu.spy")).forEach(playerMsg -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.whitelistRemove"))));
                    bukkitTask.cancel();
                    return;
                } else {
                    playersInConfig.add(player.getName());
                    this.getCustomConfig().set("Whitelisted-Players", playersInConfig);
                    this.saveCustomConfig();
                    Bukkit.getOnlinePlayers().stream().filter(playerMsg -> player.hasPermission("abu.spy")).forEach(playerMsg -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.whitelistAdd"))));
                    System.out.println("[AntiBot-Ultra] -> Player " + player.getName() + " was added to the whitelist");
                    return;
                }
                //System.out.println("Player2");
            }, configHandler.getCustomConfig().getLong("Whitelist.add-delay"));
        }
        return false;
    }

    // Not tested
    public boolean removeWhitelist(Player player) {
        if (!configHandler.getCustomConfig().getBoolean("Protection.enabled"))
            return false;
        if (player.isOp()) {
            System.out.println("[AntiBot-Ultra] -> Someone just tried to remove an operator from the whitelist!");
            return false;
        }
        playersInConfig.remove(player.getName());
        this.getCustomConfig().set("Whitelisted-Players", playersInConfig);
        Bukkit.getOnlinePlayers().stream().filter(playerMsg -> player.hasPermission("abu.spy")).forEach(playerMsg -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.whitelistRemove"))));
        this.saveCustomConfig();
        //System.out.println("[AntiBot-Ultra] -> Player " + player.getName() + " was removed from the whitelist!");
        return true;
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public List<String> getPlayersInConfig() {
        return playersInConfig;
    }

    public boolean getWhitelistActive() {
        return whitelistActive;
    }

    public void setWhitelistActive(boolean isWhitelistActive) {
        whitelistActive = isWhitelistActive;
    }

}
