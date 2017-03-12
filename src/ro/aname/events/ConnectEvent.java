package ro.aname.events;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import ro.aname.AntiBotUltra;
import ro.aname.ConfigHandler;
import ro.aname.utils.Proxy;
import ro.aname.utils.Updater;
import ro.aname.utils.WhitelistManager;

import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;

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

public class ConnectEvent implements Listener {

    WhitelistManager whitelistManager = new WhitelistManager();
    ConfigHandler configHandler = new ConfigHandler();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        Player p = event.getPlayer();
        String IP = event.getAddress().getHostAddress();
        if (configHandler.getCustomConfig().getBoolean("NickProtection.enabled")) {
            if (p.getName().toLowerCase().contains("vps_bot_") || p.getName().toLowerCase().contains("vps_bot") || p.getName().toLowerCase().contains("bot_vps") || p.getName().toLowerCase().contains("mcspam")) {
                event.disallow(KICK_OTHER, ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.nickKick")));
            }
        }
        if (Proxy.isProxy(IP)) {
            if (p.hasPermission("abu.bypass") || p.isOp()) return;
            event.disallow(KICK_OTHER, ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.proxyKick")));
        }
        if (!whitelistManager.getWhitelistActive()) whitelistManager.setWhitelist(p);

        if (whitelistManager.getWhitelistActive()) {
            if (!whitelistManager.isOnWhitelist(p)) {
                event.disallow(KICK_OTHER, ChatColor.translateAlternateColorCodes('&', configHandler.getCustomConfig().getString("Messages.whitelistKick")));
            }
            if (whitelistManager.isOnWhitelist(p)) event.allow();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void count(PlayerLoginEvent event) {
        Player p = event.getPlayer();
        if (configHandler.getCustomConfig().getBoolean("Protection.enabled")) {
            if (!whitelistManager.isOnWhitelist(p)) {
                int value = AntiBotUltra.getInstance().getHbot().get("LoginCount");
                AntiBotUltra.getInstance().getHbot().put("LoginCount", value + 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChatEvent(AsyncPlayerChatEvent event) {
        if (configHandler.getCustomConfig().getBoolean("ChatProtection.enabled")) {
            if (event.getMessage().toLowerCase().contains("mcspam") || event.getMessage().toLowerCase().contains("join server") || event.getMessage().toLowerCase().contains("connect server") || event.getMessage().toLowerCase().matches("\\d*\\.?\\d+")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes( '&', configHandler.getCustomConfig().getString("Messages.chatDisable")));
            }
        }
        return;
    }

}
