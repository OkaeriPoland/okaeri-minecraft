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

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.ErrorResponse;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.bukkit.vote.AwaitingVote;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.commons.i18n.BI18n;
import org.bukkit.command.CommandSender;

import java.util.Set;
import java.util.stream.Collectors;

@Permission("openvote.vote")
@ServiceDescriptor(label = "vote", aliases = "glosuj", description = "!commands-vote-list-description")
public class VoteCommand implements CommandService {

    @Inject private OpenVoteConfig config;
    @Inject private OpenVoteMessages messages;
    @Inject("awaitingVotes") private Set<AwaitingVote> awaitingVotes;
    @Inject private BI18n i18n;

    @Executor(pattern = {"list", "lists"}, description = "!commands-vote-list-description")
    public Message lists(CommandSender sender) {
        return this.i18n.get(sender, this.messages.getCommandsVoteListTemplate())
                .with("entries", this.config.getLists().stream()
                        .map(list -> this.i18n.get(sender, this.messages.getCommandsVoteListEntry())
                                .with("list", list)
                                .apply())
                        .collect(Collectors.joining("\n")));
    }

    @Executor(pattern = "*", description = "!commands-vote-vote-description")
    public BukkitResponse vote(@Arg("list") String list) {
        return ErrorResponse.of("Voting not implemented!");
    }
}
