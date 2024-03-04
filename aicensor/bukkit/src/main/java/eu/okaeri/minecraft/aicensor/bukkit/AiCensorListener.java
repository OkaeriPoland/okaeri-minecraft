/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2023 Okaeri, Dawid Sawicki
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
package eu.okaeri.minecraft.aicensor.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.aicensor.shared.AiCensorMessages;
import eu.okaeri.minecraft.aicensor.shared.AiCensorResponse;
import eu.okaeri.minecraft.aicensor.shared.config.AiCensorConfig;
import eu.okaeri.minecraft.aicensor.shared.config.AiCensorFailMode;
import eu.okaeri.platform.core.annotation.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class AiCensorListener implements Listener {

    private @Inject AiCensorConfig config;
    private @Inject AiCensorMessages messages;
    private @Inject AiCensorBukkit aicensor;

    @EventHandler(ignoreCancelled = true)
    public void handleChat(AsyncPlayerChatEvent event) {

        if (!this.config.getCheck().getChat().isEnabled()) {
            return;
        }

        Map<String, String> additionalVariables = new HashMap<>();
        additionalVariables.put("type", "Chat");
        additionalVariables.put("player.name", event.getPlayer().getName());

        if (event.getPlayer().getAddress() != null) {
            additionalVariables.put("player.address", event.getPlayer().getAddress().getAddress().getHostAddress());
        }

        AiCensorResponse response = this.aicensor.shouldBeBlocked(
            event.getMessage(),
            Collections.singletonMap("nick", event.getPlayer().getName())
        );

        if (response.isFail() && (this.config.getClient().getFail() == AiCensorFailMode.PREVENT)) {
            event.setCancelled(true);
            // TODO: fail message
            return;
        }

        if (response.isFail() || response.isClear()) {
            return;
        }

        event.setCancelled(true);
        // TODO: message

        this.aicensor.punish(event.getPlayer(), response);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void handleNickname(AsyncPlayerPreLoginEvent event) {

        if (!this.config.getCheck().getChat().isEnabled()) {
            return;
        }

        Map<String, String> additionalVariables = new HashMap<>();
        additionalVariables.put("type", "Nickname");
        additionalVariables.put("player.name", event.getName());
        additionalVariables.put("player.address", event.getAddress().getHostAddress());

        AiCensorResponse response = this.aicensor.shouldBeBlocked(
            event.getName(),
            this.config.getClient().getMode() + "+unbound",
            additionalVariables
        );

        if (response.isFail() || response.isClear()) {
            return;
        }

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        event.setKickMessage("x"); // TODO
    }
}
