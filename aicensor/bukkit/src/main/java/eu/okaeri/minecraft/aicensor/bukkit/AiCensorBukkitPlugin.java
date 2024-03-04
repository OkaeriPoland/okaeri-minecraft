/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2023 Okaeri, Dawid Sawicki
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
package eu.okaeri.minecraft.aicensor.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.aicensor.shared.config.AiCensorConfig;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.sdk.aicensor.AiCensorClient;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@Getter
@Setter(AccessLevel.PROTECTED)
@Scan(
    value = "eu.okaeri.minecraft.aicensor.shared",
    deep = true
)
@Scan(
    value = "eu.okaeri.minecraft.aicensor",
    exclusions = {
        "eu.okaeri.minecraft.aicensor.lib",
        "eu.okaeri.minecraft.aicensor.shared"
    },
    deep = true
)
public class AiCensorBukkitPlugin extends OkaeriBukkitPlugin {

    private @Inject AiCensorClient client;
    private @Inject AiCensorBukkit aicensor;

    @Bean("client")
    private AiCensorClient configureClient(AiCensorBukkitPlugin plugin, AiCensorConfig config) {

        if (!config.getClient().isConfigured()) {
            plugin.getLogger().log(Level.SEVERE, "Configuration value for 'secret' has to be configured. Update ./plugins/AiCensor/config.yml and use /aicensor reload or restart the server!");
            ConfigurationNotifier notifier = new ConfigurationNotifier(plugin, config);
            plugin.getServer().getPluginManager().registerEvents(notifier, plugin);
            plugin.getServer().getScheduler().runTaskTimer(plugin, notifier::broadcast, 5 * 20, 60 * 20);
        }

        return config.getClient().create();
    }

    @AllArgsConstructor
    private static class ConfigurationNotifier implements Listener {

        private static final String MESSAGE = ChatColor.RED + "AiCensor requires configuration. Update ./plugins/AiCensor/config.yml and use /aicensor reload or restart the server!";
        private final JavaPlugin plugin;
        private final AiCensorConfig config;

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent event) {
            if (this.config.getClient().isConfigured()) {
                return;
            }
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.messagePlayer(event.getPlayer()), 5 * 20);
        }

        public void messagePlayer(Player player) {
            if (!player.isOp() && !player.hasPermission("aicensor.notify")) {
                return;
            }
            player.sendMessage(MESSAGE);
        }

        public void broadcast() {
            if (this.config.getClient().isConfigured()) {
                return;
            }
            Bukkit.getOnlinePlayers().forEach(this::messagePlayer);
            this.plugin.getLogger().severe(MESSAGE);
        }
    }
}
