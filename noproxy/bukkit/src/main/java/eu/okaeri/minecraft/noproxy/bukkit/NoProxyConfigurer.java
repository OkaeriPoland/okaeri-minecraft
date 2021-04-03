package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Order;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.sdk.noproxy.NoProxyClient;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;

public class NoProxyConfigurer {

    @Bean
    @Order(1)
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
    @Order(2)
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
