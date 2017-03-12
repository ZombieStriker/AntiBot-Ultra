package ro.aname.utils;

import ro.aname.AntiBotUltra;
import ro.aname.ConfigHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

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

public class Proxy {

    private static ConfigHandler configHandler = new ConfigHandler();

    public static boolean isProxy(String IP) {
        if ((IP.equals("127.0.0.1") || (IP.matches("192\\\\.168\\\\.[01]\\\\.[0-9]{1,3}")))) return false;
        for (String s : AntiBotUltra.getInstance().getBlacklists().keySet()) {
            try {
                String res = "";
                Scanner scanner = new Scanner(new URL(s.replace(",", ".") + IP).openStream());
                while (scanner.hasNextLine()) {
                    res = res + scanner.nextLine();
                }
                String[] args = ((String) AntiBotUltra.getInstance().getBlacklists().get(s)).split(",");
                for (String arg : args) {
                    if (res.matches(arg)) {
                        return true;
                    }
                    scanner.close();
                }
            } catch (MalformedURLException e) {
                System.out.println("Error scanning --> " + IP + ".");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error scanning --> " + IP + "");
                e.printStackTrace();
            }
        }
        return false;
    }

}
