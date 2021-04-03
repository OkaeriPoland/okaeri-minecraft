package eu.okaeri.minecraft.openvote.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import net.md_5.bungee.api.ChatColor;

public class OpenVoteMessager {

    @Inject private OpenVoteMessages messages;

    public String format(String message, String... params) {
        message = message.replace("{PREFIX}", this.messages.getPrefix());
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < params.length; i++) {
            message = message.replace("{" + i + "}", params[i]);
        }
        return message;
    }
}
