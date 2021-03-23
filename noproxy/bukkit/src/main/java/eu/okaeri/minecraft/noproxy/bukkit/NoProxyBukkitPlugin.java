package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.configs.bukkit.BukkitConfigurer;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.sdk.noproxy.NoProxyClient;
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
public class NoProxyBukkitPlugin extends JavaPlugin {

    private NoProxyBukkit noproxy;
    private NoProxyClient client;
    private NoProxyConfig configuration;

    private class ConfigurationNotifier implements Listener {

        private final NoProxyBukkitPlugin plugin;
        private String updateMessage = ChatColor.RED + NoProxyBukkitPlugin.super.getName() + " requires configuration. See config.yml for details!";

        public ConfigurationNotifier(NoProxyBukkitPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent event) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.messagePlayer(event.getPlayer()), 5 * 20);
        }

        public void messagePlayer(Player player) {
            if (!player.isOp() && !player.hasPermission("noproxy.notify")) {
                return;
            }
            player.sendMessage(this.updateMessage);
        }

        public void messageAdmins() {
            Bukkit.getOnlinePlayers().forEach(this::messagePlayer);
        }

        public void logToConsole() {
            Bukkit.getConsoleSender().sendMessage(this.updateMessage);
        }
    }

    @Override
    public void onEnable() {

        // initialize configuration
        NoProxyConfig config;
        try {
            config = (NoProxyConfig) new NoProxyConfig()
                    .withBindFile(new File(this.getDataFolder(), "config.yml"))
                    .withConfigurer(new BukkitConfigurer())
                    .saveDefaults()
                    .load(true);
        } catch (Exception exception) {
            this.getLogger().log(Level.SEVERE, "Failed to load config.yml", exception);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // validate token
        String token = config.getToken();
        if ((token == null) || "".equals(token)) {
            this.getLogger().log(Level.SEVERE, "Configuration value for 'token' was not found in the config.yml. Please validate your config and restart the server.");
            ConfigurationNotifier notifier = new ConfigurationNotifier(this);
            this.getServer().getPluginManager().registerEvents(notifier, this);
            this.getServer().getScheduler().runTaskTimer(this, notifier::messageAdmins, 20, 60 * 20);
            this.getServer().getScheduler().runTaskLater(this, notifier::logToConsole, 5 * 20);
            return;
        }

        // create context
        this.client = new NoProxyClient(token);

        // create noproxy
        this.noproxy = new NoProxyBukkit(this);

        // webhook config
        config.getWebhooks().forEach(this.noproxy::addWebhook);

        // listeners
        this.getServer().getPluginManager().registerEvents(new NoProxyListener(this), this);
    }

    protected String message(String message, String... params) {
        message = message.replace("{PREFIX}", this.configuration.getMessages().getPrefix());
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < params.length; i++) {
            message = message.replace("{" + i + "}", params[i]);
        }
        return message;
    }
}
