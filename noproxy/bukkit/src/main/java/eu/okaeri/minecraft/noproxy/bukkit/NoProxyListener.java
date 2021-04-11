/*
 * OK! No.Proxy Minecraft
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
package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.commands.bukkit.response.ColorResponse;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import eu.okaeri.platform.core.annotation.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;

@Component
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

        BaseComponent[] kickMessage = ColorResponse.of(this.messages.getPlayerInfo())
                .withField("{PREFIX}", this.messages.getPrefix())
                .render();

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        event.setKickMessage(TextComponent.toLegacyText(kickMessage));
    }
}
