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

import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.SuccessResponse;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import eu.okaeri.sdk.noproxy.NoProxyClient;

@Permission("noproxy.admin")
@ServiceDescriptor(label = "noproxy", description = "NoProxy admin command")
public class NoProxyCommand implements CommandService {

    @Inject private NoProxyConfig config;
    @Inject private NoProxyMessages messages;
    @Inject private NoProxyBukkit noproxy;
    @Inject private NoProxyBukkitPlugin plugin;

    @Executor(async = true, description = "reloads the configuration")
    public BukkitResponse reload() {

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

        return SuccessResponse.of("The configuration has been reloaded!");
    }
}
