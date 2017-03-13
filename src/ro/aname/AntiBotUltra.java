package ro.aname;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ro.aname.events.ConnectEvent;
import ro.aname.utils.Metrics;
import ro.aname.utils.Updater;
import ro.aname.utils.Website;
import ro.aname.utils.WhitelistManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class AntiBotUltra extends JavaPlugin {

    private static AntiBotUltra instance;

    private BukkitTask taskId;
    private Map<String, Object> blacklists;
    private HashMap<String, Integer> Hbot;
    private List<String> proxyList;

    private int downloadedProxies;

    private ConfigHandler configHandler;
    private WhitelistManager whitelistManager;

    private boolean pluginEnabled;

    private Updater updater;

    public static AntiBotUltra getInstance() {
        return instance;
    }

    //    commands:
//    antibot-ultra:
//    description: Your description
//    usage: /kit
    @Override
    public void onEnable() {
        instance = this;
        pluginEnabled = true;
        Bukkit.getServer().getPluginManager().registerEvents(new ConnectEvent(), AntiBotUltra.getInstance());
        this.getCommand("antibot-ultra").setExecutor(new Commands());
        Hbot = new HashMap<>();
        Hbot.put("LoginCount", 0);
        blacklists = new HashMap<>();
        proxyList = new ArrayList<>();
        blacklists.put("http://botscout,com/test/?ip=", "Y");
        blacklists.put("http://api,stopforumspam,org/api?ip=", "<appears>yes</appears>");
        configHandler = new ConfigHandler();
        whitelistManager = new WhitelistManager();
        whitelistManager.saveDefaultConfig();
        configHandler.saveDefaultConfig();
        configHandler.getCustomConfig().getConfigurationSection("Proxy-List").getValues(true);
        new Metrics(this);
        updater = new Updater(this, "22933");
        Updater.UpdateResults result = updater.checkForUpdates();
        if (result.getResult() == Updater.UpdateResult.FAIL) {
            if (configHandler.getCustomConfig().getBoolean("Debug.enabled")) {
                System.out.println("[AntiBot-Ultra] -> Failed to check for updates!");
                System.out.println("[AntiBot-Ultra] -> Stacktrace: " + result.getVersion());
            }
        } else if (result.getResult() == Updater.UpdateResult.NO_UPDATE) {
            if (configHandler.getCustomConfig().getBoolean("Debug.enabled")) {
                System.out.println("[AntiBot-Ultra] -> There are no new updates!");
            }
        } else if (result.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
            if (configHandler.getCustomConfig().getBoolean("Debug.enabled")) {
                System.out.println("[AntiBot-Ultra] -> There are new updates!");
                System.out.println("[AntiBot-Ultra] -> New version: " + result.getVersion());
                System.out.println("[AntiBot-Ultra] -> Visit 'https://www.spigotmc.org/resources/antibot-ultra.22933/' to download " + result.getVersion() + " !");
            }
        }
        downloadProxies();
        Website.readMessage();
        protection();
    }

    @Override
    public void onDisable() {
        Hbot.clear();
        //if (Bukkit.getScheduler().isCurrentlyRunning(whitelistManager.bukkitTask.getTaskId())) whitelistManager.bukkitTask.cancel();
        //if (Bukkit.getScheduler().isCurrentlyRunning(taskId.getTaskId())) taskId.cancel();
        whitelistManager.getPlayersInConfig().clear();
        getBlacklists().clear();
        getHbot().clear();
        getProxyList().clear();
        getServer().getScheduler().cancelTasks(AntiBotUltra.getInstance());
    }

    public void downloadProxies() {
        if (configHandler.getCustomConfig().getBoolean("Proxy.download")) {
            if (configHandler.getCustomConfig().getBoolean("Debug.enabled")) {
                System.out.println("[AntiBot-Ultra] Downloading proxies in 5 seconds!");
            }
            Bukkit.getScheduler().runTaskLater(AntiBotUltra.getInstance(), () -> {
                URL url;
                try {
                    // get URL content

                    String a = "http://aname.dbmgaming.com/proxylist.txt";
                    url = new URL(a);
                    URLConnection conn = url.openConnection();

                    // open the stream and put it into BufferedReader
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String inputLine;
                    while ((inputLine = br.readLine()) != null) {
                        proxyList.add(inputLine);
                        downloadedProxies++;
                    }
                    br.close();
                    if (configHandler.getCustomConfig().getBoolean("Debug.enabled")) {
                        System.out.println("[AntiBot-Ultra] -> Downloaded " + downloadedProxies + " proxies!");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 100L);
        }
    }

    private void protection() {
        if (configHandler.getCustomConfig().getBoolean("Protection.enabled")) {
            taskId = Bukkit.getScheduler().runTaskTimer(AntiBotUltra.getInstance(), () -> {
                if (Hbot.get("LoginCount") >= getConfig().getInt("Protection.sensibility")) {
                    if ((Hbot.get("LoginCount") > getConfig().getInt("Protection.sensibility")) && !whitelistManager.getWhitelistActive()) {
                        whitelistManager.setWhitelistActive(true);
                        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("abu.spy")).forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.attackDetected"))));
                        Bukkit.getScheduler().runTaskLater(AntiBotUltra.getInstance(), () -> {
                            Hbot.put("LoginCount", 0);
                            Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("abu.spy")).forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.attackAnalyze"))));
                        }, configHandler.getCustomConfig().getLong("Protection.runTaskLater"));
                        Bukkit.getScheduler().runTaskLater(AntiBotUltra.getInstance(), () -> {
                            if (Hbot.get("LoginCount") < getConfig().getInt("Protection.sensibility")) {
                                whitelistManager.setWhitelistActive(false);
                                Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("abu.spy")).forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.attackFinish"))));
                            } else {
                                Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("abu.spy")).forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.attackPersists"))));
                                protection();
                                taskId.cancel();
                            }
                        }, configHandler.getCustomConfig().getLong("Protection.runTaskLater2"));
                    }
                }
                Hbot.put("LoginCount", 0);
            }, configHandler.getCustomConfig().getLong("Protection.runTaskTimer"), configHandler.getCustomConfig().getLong("Protection.runTaskTimer2"));
        }
    }

    public Map<String, Object> getBlacklists() {
        return blacklists;
    }

    public BukkitTask getTaskId() {
        return taskId;
    }

    public HashMap<String, Integer> getHbot() {
        return Hbot;
    }

    public List<String> getProxyList() {
        return proxyList;
    }

    public int getDownloadedProxies() {
        return downloadedProxies;
    }

    public Updater getUpdater() {
        return updater;
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public void setPluginEnabled(boolean enabled) {
        pluginEnabled = enabled;
    }
}
