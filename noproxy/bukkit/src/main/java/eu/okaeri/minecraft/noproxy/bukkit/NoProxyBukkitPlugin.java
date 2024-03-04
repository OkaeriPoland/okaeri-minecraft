/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2021 Okaeri, Dawid Sawicki
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.sdk.noproxy.NoProxyClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.logging.Level;

@Getter
@Setter(AccessLevel.PROTECTED)
@Register(NoProxyConfig.class) // load config
@Register(NoProxyMessages.class) // load messages
@Register(NoProxyCommand.class) // register admin command
@Register(NoProxyListener.class) // register listener
//@Scan(
//        value = "eu.okaeri.minecraft.noproxy",
//        exclusions = "eu.okaeri.minecraft.noproxy.lib",
//        deep = true
//)
public class NoProxyBukkitPlugin extends OkaeriBukkitPlugin {

    private @Inject NoProxyClient client;
    private @Inject NoProxyBukkit noproxy;

    public NoProxyBukkitPlugin() {
    }

    public NoProxyBukkitPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Bean
    private NoProxyClient configureClient(NoProxyBukkitPlugin plugin, NoProxyConfig config) {

        String token = config.getToken();
        if (!token.isEmpty()) {
            return new NoProxyClient(token);
        }

        plugin.getLogger().log(Level.SEVERE, "Configuration value for 'token' was not found in the config.yml. Please validate your config and restart the server.");
        ConfigurationNotifier notifier = new ConfigurationNotifier(plugin);
        plugin.getServer().getPluginManager().registerEvents(notifier, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, notifier::broadcast, 5 * 20, 60 * 20);
        throw new BreakException("Failed to initialize NoProxy, token not set");
    }

    @Bean
    private NoProxyBukkit configureService(NoProxyBukkitPlugin plugin, NoProxyConfig config, NoProxyClient client) {
        NoProxyBukkit noproxy = new NoProxyBukkit(plugin, client);
        config.getWebhooks().forEach(noproxy::addWebhook);
        return noproxy;
    }

    @AllArgsConstructor
    private static class ConfigurationNotifier implements Listener {

        private static final String MESSAGE = ChatColor.RED + "NoProxy requires configuration. Update config.yml and restart the server!";
        private final org.bukkit.plugin.java.JavaPlugin plugin;

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent event) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.messagePlayer(event.getPlayer()), 5 * 20);
        }

        public void messagePlayer(Player player) {
            if (!player.isOp() && !player.hasPermission("noproxy.notify")) {
                return;
            }
            player.sendMessage(MESSAGE);
        }

        public void broadcast() {
            Bukkit.getOnlinePlayers().forEach(this::messagePlayer);
            Bukkit.getConsoleSender().sendMessage(MESSAGE);
        }
    }
}
