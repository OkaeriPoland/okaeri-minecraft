package eu.okaeri.minecraft.openvote.bukkit;

import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Order;
import eu.okaeri.sdk.openvote.OpenVoteClient;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class OpenVoteConfigurer {

    @Bean
    @Order(1)
    private OpenVoteClient configureClient() {
        return new OpenVoteClient();
    }

    @Bean
    @Order(2)
    private OpenVoteBukkit configureService(OpenVoteConfig config, OpenVoteBukkitPlugin plugin, OpenVoteClient client) {

        // validate name
        if (OpenVoteConfig.PLACEHOLDER_SERVER.equals(config.getServer())) {

            plugin.getLogger().log(Level.SEVERE, "Configuration value for 'server' was not changed in the config.yml. Please update your config and run /openvote reload!");
            ConfigurationNotifier notifier = new ConfigurationNotifier(plugin, config);
            plugin.getServer().getPluginManager().registerEvents(notifier, plugin);

            plugin.getServer().getScheduler().runTaskTimer(plugin, new BukkitRunnable() {
                @Override
                public void run() {
                    if (OpenVoteConfig.PLACEHOLDER_SERVER.equals(config.getServer())) {
                        notifier.broadcast();
                        return;
                    }
                    this.cancel();
                }
            }, 5 * 20, 60 * 20);
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
}
