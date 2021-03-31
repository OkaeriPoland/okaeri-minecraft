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

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
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

import java.io.File;
import java.util.logging.Level;

@Getter
public class OpenVoteBukkitPlugin extends JavaPlugin {

    private OpenVoteBukkit openvote;
    private OpenVoteClient client;
    private OpenVoteConfig configuration;
    private OpenVoteMessages messages;
    private Injector injector = OkaeriInjector.create();

    @Override
    public void onEnable() {

        // initialize configuration
        try {
            this.configuration = ConfigManager.create(OpenVoteConfig.class, (it) -> {
                it.withBindFile(new File(this.getDataFolder(), "config.yml"));
                it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true));
                it.saveDefaults();
                it.load(true);
            });
            this.messages = ConfigManager.create(OpenVoteMessages.class, (it) -> {
                it.withBindFile(new File(this.getDataFolder(), "messages.yml"));
                it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true));
                it.saveDefaults();
                it.load(true);
            });
        } catch (Exception exception) {
            this.getLogger().log(Level.SEVERE, "Failed to load configuration", exception);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // validate name
        if (OpenVoteConfig.PLACEHOLDER_SERVER.equals(this.configuration.getServer())) {
            this.getLogger().log(Level.SEVERE, "Configuration value for 'server' was not changed in the config.yml. Please validate your config and restart the server.");
            ConfigurationNotifier notifier = new ConfigurationNotifier(this);
            this.getServer().getPluginManager().registerEvents(notifier, this);
            this.getServer().getScheduler().runTaskTimer(this, notifier::broadcast, 5 * 20, 60 * 20);
            return;
        }

        // initialize client/service
        this.client = new OpenVoteClient();
        this.openvote = new OpenVoteBukkit(this);
        if (this.configuration.isEnableWebhooks()) this.configuration.getWebhooks().forEach(this.openvote::addWebhook);

        // register injectables
        this.injector.registerInjectable(this);
        this.injector.registerInjectable(this.configuration);
        this.injector.registerInjectable(this.messages);
        this.injector.registerInjectable(this.openvote);
    }

    @AllArgsConstructor
    private static class ConfigurationNotifier implements Listener {

        private static final String MESSAGE = ChatColor.RED + "OpenVote requires configuration. See config.yml for details!";
        private final JavaPlugin plugin;

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent event) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.messagePlayer(event.getPlayer()), 5 * 20);
        }

        public void messagePlayer(Player player) {
            if (!player.isOp() && !player.hasPermission("openvote.notify")) {
                return;
            }
            player.sendMessage(MESSAGE);
        }

        public void broadcast() {
            Bukkit.getOnlinePlayers().forEach(this::messagePlayer);
            Bukkit.getConsoleSender().sendMessage(MESSAGE);
        }
    }

    protected String message(String message, String... params) {
        message = message.replace("{PREFIX}", this.messages.getPrefix());
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < params.length; i++) {
            message = message.replace("{" + i + "}", params[i]);
        }
        return message;
    }
}
