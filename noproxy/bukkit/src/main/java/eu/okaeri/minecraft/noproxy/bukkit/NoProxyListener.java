package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;

public class NoProxyListener implements Listener {

    @Inject private NoProxyConfig config;
    @Inject private NoProxyMessages messages;
    @Inject private NoProxyBukkit noproxy;

    @EventHandler(priority = EventPriority.LOW)
    public void handlePreLogin(AsyncPlayerPreLoginEvent event) {

        String name = event.getName();
        String address = event.getAddress().getHostAddress();

        List<String> whitelist = this.config.getWhitelist();
        if (whitelist.contains(name) || whitelist.contains(address)) {
            return;
        }

        if (!this.noproxy.shouldBeBlocked(address, name)) {
            return;
        }

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        event.setKickMessage(this.messages.getPlayerInfo());
    }
}
