package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import eu.okaeri.platform.core.annotation.Component;
import net.md_5.bungee.api.ChatColor;

@Component
public class NoProxyMessager {

    @Inject private NoProxyMessages messages;

    public String format(String message, String... params) {
        message = message.replace("{PREFIX}", this.messages.getPrefix());
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < params.length; i++) {
            message = message.replace("{" + i + "}", params[i]);
        }
        return message;
    }
}
