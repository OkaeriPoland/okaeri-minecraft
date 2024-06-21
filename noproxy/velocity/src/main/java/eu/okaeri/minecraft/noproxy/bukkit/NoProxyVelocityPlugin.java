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

import com.velocitypowered.api.plugin.Plugin;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
import eu.okaeri.sdk.noproxy.NoProxyClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
@Scan(
    value = "eu.okaeri.minecraft.noproxy",
    exclusions = "eu.okaeri.minecraft.noproxy.lib",
    deep = true
)
@Plugin(id = "noproxy",
    name = "No.Proxy",
    description = "OK! No.Proxy Minecraft (Velocity)",
    version = "2.0",
    authors = {"Dawid Sawicki <dawid@okaeri.cloud>"}
)
public class NoProxyVelocityPlugin extends OkaeriVelocityPlugin {

    private NoProxyClient client;
    private NoProxyVelocity noproxy;

    @Bean
    private NoProxyClient configureClient(NoProxyVelocityPlugin plugin, NoProxyConfig config) {

        String token = config.getToken();
        if (!token.isEmpty()) {
            this.client = new NoProxyClient(token);
            return this.client;
        }

        plugin.getLogger().error("Configuration value for 'token' was not found in the config.yml. Please validate your config and restart the server.");
        throw new BreakException("Failed to initialize NoProxy, token not set");
    }

    @Bean
    private NoProxyVelocity configureService(NoProxyVelocityPlugin plugin, NoProxyConfig config, NoProxyClient client) {
        this.noproxy = new NoProxyVelocity(plugin, client);
        config.getWebhooks().forEach(this.noproxy::addWebhook);
        return this.noproxy;
    }
}
