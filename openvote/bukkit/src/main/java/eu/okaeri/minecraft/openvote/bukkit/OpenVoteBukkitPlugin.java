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
package eu.okaeri.minecraft.openvote.bukkit;

import eu.okaeri.minecraft.openvote.bukkit.command.OpenVoteCommand;
import eu.okaeri.minecraft.openvote.bukkit.command.VoteCommand;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.WithBean;
import eu.okaeri.sdk.openvote.OpenVoteClient;
import lombok.Getter;

@Getter
@WithBean(OpenVoteConfig.class) // load config
@WithBean(OpenVoteConfigurer.class) // check config and create client/openvote
@WithBean(OpenVoteMessages.class) // load messages
@WithBean(OpenVoteMessager.class) // create messager helper
@WithBean(OpenVoteCommand.class) // create admin command
@WithBean(VoteCommand.class) // create user command
public class OpenVoteBukkitPlugin extends OkaeriBukkitPlugin {
    // expose as api
    private OpenVoteBukkit openvote;
    private OpenVoteClient client;
}
