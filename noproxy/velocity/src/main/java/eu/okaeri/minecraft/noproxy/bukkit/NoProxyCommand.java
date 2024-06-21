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

import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.velocity.annotation.Permission;
import eu.okaeri.commands.velocity.response.VelocityResponse;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import eu.okaeri.sdk.noproxy.NoProxyClient;

@Permission("noproxy.admin")
@Command(label = "noproxy", description = "NoProxy admin command")
public class NoProxyCommand implements CommandService {

    private @Inject NoProxyConfig config;
    private @Inject NoProxyMessages messages;
    private @Inject NoProxyVelocity noproxy;
    private @Inject NoProxyVelocityPlugin plugin;

    @Executor(description = "reloads the configuration")
    public VelocityResponse reload() {

        try {
            this.config.load();
            this.messages.load();
        } catch (OkaeriException exception) {
            throw new CommandException("Reload failed. See more in the console.", exception);
        }

        String token = this.config.getToken();
        if (!token.isEmpty()) {
            NoProxyClient client = new NoProxyClient(token);
            this.noproxy.setClient(client);
            this.plugin.setClient(client);
        }

        return VelocityResponse.ok("The configuration has been reloaded!");
    }
}
