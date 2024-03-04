/*
 * OK! OpenVote Minecraft
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
package eu.okaeri.minecraft.openvote.bukkit.command;

import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.bukkit.annotation.Async;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import org.bukkit.command.CommandSender;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Async
@Permission("openvote.admin")
@Command(label = "openvote", description = "OpenVote admin command")
public class OpenVoteCommand implements CommandService {

    private @Inject OpenVoteConfig config;
    private @Inject OpenVoteMessages messages;
    private @Inject Logger logger;
    private @Inject BI18n i18n;

    @Executor(description = "${commandsOpenvoteReloadDescription}")
    public Message reload(CommandSender sender) {

        try {
            this.config.load();
            this.i18n.load();
        } catch (OkaeriException exception) {
            this.logger.log(Level.SEVERE, "Failed to reload configuration", exception);
            return this.i18n.get(sender, this.messages.getCommandsOpenvoteReloadFail());
        }

        return this.i18n.get(sender, this.messages.getCommandsOpenvoteReloadSuccess());
    }

    @Permission("openvote.admin.reset")
    @Executor(description = "${commandsOpenvoteResetDescription}")
    public Message reset(CommandSender sender) {

        this.config.setStatsId(UUID.randomUUID());
        this.config.save();

        return this.i18n.get(sender, this.messages.getCommandsOpenvoteResetSuccess());
    }
}
