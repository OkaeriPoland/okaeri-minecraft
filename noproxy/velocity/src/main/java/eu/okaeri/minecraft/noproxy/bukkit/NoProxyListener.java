/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2024 Okaeri, Dawid Sawicki
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

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import eu.okaeri.commands.velocity.response.ColorResponse;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import eu.okaeri.platform.core.annotation.Service;
import eu.okaeri.platform.velocity.component.type.listener.Listener;
import net.kyori.adventure.text.Component;

import java.util.List;

@Service
public class NoProxyListener implements Listener {

    private @Inject NoProxyConfig config;
    private @Inject NoProxyMessages messages;
    private @Inject NoProxyVelocity noproxy;

    @Subscribe(order = PostOrder.EARLY)
    public void handlePreLogin(PreLoginEvent event) {

        String name = event.getUsername();
        String address = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

        List<String> whitelist = this.config.getWhitelist();
        if (whitelist.contains(name) || whitelist.contains(address)) {
            return;
        }

        if (!this.noproxy.shouldBeBlocked(address, name)) {
            return;
        }

        Component kickMessage = ColorResponse.of(this.messages.getPlayerInfo())
                .with("{PREFIX}", this.messages.getPrefix())
                .render();

        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(kickMessage));
    }
}
