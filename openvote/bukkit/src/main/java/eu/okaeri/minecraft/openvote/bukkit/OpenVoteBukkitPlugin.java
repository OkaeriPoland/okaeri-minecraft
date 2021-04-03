/*
 * OK! OpenVote Minecraft
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
package eu.okaeri.minecraft.openvote.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.bukkit.command.OpenVoteCommand;
import eu.okaeri.minecraft.openvote.bukkit.command.VoteCommand;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.sdk.openvote.OpenVoteClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
@Register(OpenVoteConfig.class) // load config
@Register(OpenVoteMessages.class) // load messages
@Register(OpenVoteMessager.class) // create messager helper
@Register(OpenVoteCommand.class) // create admin command
@Register(VoteCommand.class) // create user command
public class OpenVoteBukkitPlugin extends OkaeriBukkitPlugin {

    @Inject private OpenVoteBukkit openvote;
    @Inject private OpenVoteClient client;

    @Bean
    private OpenVoteClient configureClient() {
        return new OpenVoteClient();
    }

    @Bean
    private OpenVoteBukkit configureService(OpenVoteConfig config, OpenVoteBukkitPlugin plugin, OpenVoteClient client) {

        // validate name
        if (OpenVoteConfig.PLACEHOLDER_SERVER.equals(config.getServer())) {
            plugin.getLogger().log(Level.SEVERE, "Configuration value for 'server' was not changed in the config.yml. Please update your config and run /openvote reload!");
            ConfigurationNotifier notifier = new ConfigurationNotifier(plugin, config);
            plugin.getServer().getPluginManager().registerEvents(notifier, plugin);
            plugin.getServer().getScheduler().runTaskTimer(plugin, notifier::broadcast, 5 * 20, 60 * 20);
        }

        OpenVoteBukkit openvote = new OpenVoteBukkit(plugin, client);
        if (config.isEnableWebhooks()) config.getWebhooks().forEach(openvote::addWebhook);
        return openvote;
    }

    @AllArgsConstructor
    private static class ConfigurationNotifier implements Listener {

        private static final String MESSAGE = ChatColor.RED + "OpenVote requires configuration. Update config.yml and run /openvote reload!";
        private final JavaPlugin plugin;
        private final OpenVoteConfig config;

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent event) {
            if (!OpenVoteConfig.PLACEHOLDER_SERVER.equals(this.config.getServer())) {
                return;
            }
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.messagePlayer(event.getPlayer()), 5 * 20);
        }

        public void messagePlayer(Player player) {
            if (!player.isOp() && !player.hasPermission("openvote.notify")) {
                return;
            }
            player.sendMessage(MESSAGE);
        }

        public void broadcast() {
            if (!OpenVoteConfig.PLACEHOLDER_SERVER.equals(this.config.getServer())) {
                return;
            }
            Bukkit.getOnlinePlayers().forEach(this::messagePlayer);
            Bukkit.getConsoleSender().sendMessage(MESSAGE);
        }
    }
}
