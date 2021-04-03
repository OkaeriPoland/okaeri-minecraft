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
package eu.okaeri.minecraft.openvote.bukkit.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.ColorResponse;
import eu.okaeri.commands.bukkit.response.ErrorResponse;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;

import java.util.List;

@Permission("openvote.vote")
@ServiceDescriptor(label = "vote", aliases = "glosuj", description = "OpenVote user command")
public class VoteCommand implements CommandService {

    @Inject private OpenVoteConfig config;
    @Inject private OpenVoteMessages messages;

    @Executor(pattern = {"list", "lists"}, description = "displays all lists available for voting")
    public BukkitResponse lists() {

        List<String> lists = this.config.getLists();
        StringBuilder out = new StringBuilder("&aUse /vote &e<list>&a with one of the following:\n");

        for (String list : lists) {
            out.append("&7- &e").append(list).append("\n");
        }

        return ColorResponse.of(out.toString());
    }

    @Executor(pattern = "*", description = "starts voting process on a specific list")
    public BukkitResponse vote(@Arg("list") String list) {
        return ErrorResponse.of("Voting not implemented!");
    }
}
