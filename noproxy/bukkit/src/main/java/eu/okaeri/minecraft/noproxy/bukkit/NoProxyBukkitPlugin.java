package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.minecraft.noproxy.shared.NoProxyWebhook;
import eu.okaeri.sdk.noproxy.NoProxyClient;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Getter
public class NoProxyBukkitPlugin extends JavaPlugin {

    private NoProxyBukkit noproxy;
    private NoProxyClient client;

    class ConfigurationNotifier implements Listener {

        private final NoProxyBukkitPlugin plugin;
        private String updateMessage = ChatColor.RED + "Plugin " + NoProxyBukkitPlugin.super.getName()
                + " requires configuration. See config.yml for more details!";

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

        // save default configuration if config.yml does not exists
        this.saveDefaultConfig();

        // validate configuration and create ApiContext
        FileConfiguration config = this.getConfig();
        String token = config.getString("token");

        if ((token == null) || "".equals(token)) {
            this.getLogger().log(Level.SEVERE, "Configuration value for 'token' was not found in the config.yml. Please validate your config and restart the server.");
            ConfigurationNotifier notifier = new ConfigurationNotifier(this);
            this.getServer().getPluginManager().registerEvents(notifier, this);
            this.getServer().getScheduler().runTaskTimer(this, notifier::messageAdmins, 20, 60 * 20);
            this.getServer().getScheduler().runTaskLater(this, notifier::logToConsole, 5 * 20);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // create context
        this.client = new NoProxyClient(token);

        // create noproxy
        this.noproxy = new NoProxyBukkit(this);

        // webhook config
        @SuppressWarnings("unchecked") List<Map<String, Object>> webhooks = (List<Map<String, Object>>) config.getList("webhooks");
        if (webhooks != null) {
            for (Map<String, Object> webhook : webhooks) {
                NoProxyWebhook noProxyWebhook = new NoProxyWebhook();
                Object url = webhook.get("url");
                if (url == null) {
                    this.getLogger().log(Level.WARNING, "One or more of webhooks does not have 'url', skipping.");
                    continue;
                }
                noProxyWebhook.setUrl(String.valueOf(url));
                Object method = webhook.get("method");
                if (method == null) {
                    this.getLogger().log(Level.INFO, "Webhook '" + url + "' does not define 'method'. Using defaults: " + noProxyWebhook.getMethod());
                } else {
                    noProxyWebhook.setMethod(String.valueOf(method));
                }
                Object content = webhook.get("content");
                if (content != null) {
                    noProxyWebhook.setContent(String.valueOf(content));
                }
                Object blockedOnly = webhook.get("blocked-only");
                if (blockedOnly != null) {
                    noProxyWebhook.setBlockedOnly(Boolean.parseBoolean(String.valueOf(blockedOnly)));
                }
                this.noproxy.addWebhook(noProxyWebhook);
            }
        }

        // custom api url
        String apiUrl = this.getConfig().getString("api-url");
        if ((apiUrl != null) && !"".equals(apiUrl)) {
            this.client.getUnirest().config().defaultBaseUrl(apiUrl);
        }

        // listeners
        this.getServer().getPluginManager().registerEvents(new NoProxyListener(this), this);
    }

    protected String message(String key, String... params) {
        String message = this.getConfig().getString("message-" + key);
        Validate.notNull(message, "message for " + key + " not found");
        message = message.replace("{PREFIX}", this.getConfig().getString("message-prefix"));
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < params.length; i++) {
            message = message.replace("{" + i + "}", params[i]);
        }
        return message;
    }
}
