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

import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.bukkit.annotation.Async;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.aicensor.shared.AiCensorMessages;
import eu.okaeri.minecraft.aicensor.shared.config.AiCensorConfig;
import eu.okaeri.sdk.aicensor.AiCensorClient;

@Async
@Permission("aicensor.admin")
@Command(label = "aicensor", description = "AiCensor admin command")
public class AiCensorCommand implements CommandService {

    private @Inject AiCensorConfig config;
    private @Inject AiCensorMessages messages;
    private @Inject AiCensorBukkit aicensor;
    private @Inject AiCensorBukkitPlugin plugin;

    @Executor(description = "reloads the configuration")
    public BukkitResponse reload() {

        try {
            this.config.load();
            this.messages.load();
        } catch (OkaeriException exception) {
            throw new CommandException("Reload failed. See more in the console.", exception);
        }

        AiCensorClient client = this.config.getClient().create();
        this.aicensor.setClient(client);
        this.plugin.setClient(client);

        return BukkitResponse.ok("The configuration has been reloaded!");
    }
}
