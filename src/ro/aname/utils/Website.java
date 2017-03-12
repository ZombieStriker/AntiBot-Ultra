package ro.aname.utils;

import org.bukkit.Bukkit;
import ro.aname.AntiBotUltra;
import ro.aname.ConfigHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

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


public class Website {

    private static ConfigHandler configHandler = new ConfigHandler();

    public static void readMessage() {
        if (configHandler.getCustomConfig().getBoolean("Motd.enabled")) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(AntiBotUltra.getInstance(), () -> {
                try {
                    URL paste = new URL("http://aname.dbmgaming.com/AName25/motd.txt");
                    BufferedReader in = new BufferedReader(new InputStreamReader(paste.openStream()));
                    //int count = 0;
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("[AntiBot-Ultra] -> " + inputLine);
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 1, 1440 * 20);
        }
        return;
    }
}
