package eu.okaeri.minecraft.noproxy.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;

public class NoProxyListener implements Listener {

    private final NoProxyBukkitPlugin plugin;
    private final NoProxyBukkit noproxy;

    public NoProxyListener(NoProxyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.noproxy = plugin.getNoproxy();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void handlePreLogin(AsyncPlayerPreLoginEvent event) {

        String name = event.getName();
        String address = event.getAddress().getHostAddress();

        List<String> whitelist = this.plugin.getConfig().getStringList("white-list");
        if (whitelist.contains(name) || whitelist.contains(address)) {
            return;
        }

        if (!this.noproxy.shouldBeBlocked(address, name)) {
            return;
        }

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        event.setKickMessage(this.plugin.message("player-info"));
    }
}