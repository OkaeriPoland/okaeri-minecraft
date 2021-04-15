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
package eu.okaeri.minecraft.openvote.bukkit.vote;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.annotation.Timer;
import eu.okaeri.platform.bukkit.commons.command.CommandRunner;
import eu.okaeri.platform.bukkit.commons.time.MinecraftTimeEquivalent;
import eu.okaeri.sdk.openvote.OpenVoteClient;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVoteCheckRequest;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVoteCheckResult;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVoteState;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Timer(rate = MinecraftTimeEquivalent.SECONDS_30, async = true)
public class VotesUpdateTask implements Runnable {

    @Inject private OpenVoteClient client;
    @Inject private OpenVoteConfig config;
    @Inject private OpenVoteMessages messages;
    @Inject private Plugin plugin;
    @Inject private Server server;

    @Inject("awaitingVotes")
    private Set<AwaitingVote> awaitingVotes;

    @Override
    public void run() {

        if (this.awaitingVotes.isEmpty()) {
            return;
        }

        List<UUID> votesIds = this.awaitingVotes.stream()
                .map(AwaitingVote::getId)
                .collect(Collectors.toList());

        OpenVoteServerVoteCheckRequest request = new OpenVoteServerVoteCheckRequest(votesIds);
        OpenVoteServerVoteCheckResult checkResult = this.client.postServerVoteCheck(request);

        for (Map.Entry<UUID, String> voteEntry : checkResult.getVotes().entrySet()) {

            UUID voteId = voteEntry.getKey();
            String status = voteEntry.getValue();

            if (OpenVoteServerVoteState.WAIT.name().equals(status)) {
                continue;
            }

            if (OpenVoteServerVoteState.REMOVE.name().equals(status)) {
                this.awaitingVotes.removeIf(vote -> vote.getId() == voteId);
                continue;
            }

            if (OpenVoteServerVoteState.REWARD.name().equals(status)) {
                this.awaitingVotes.stream()
                        .filter(vote -> vote.getId() == voteId)
                        .findAny()
                        .ifPresent(this::reward);
            }
        }
    }

    private void reward(AwaitingVote vote) {

        Player player = this.server.getPlayer(vote.getPlayer());
        if ((player == null) || !player.isOnline()) {
            return;
        }

        this.awaitingVotes.remove(vote);
        CommandRunner.of(this.plugin, player)
                .field("name", player.getName())
                .field("uuid", String.valueOf(player.getUniqueId()))
                .field("list", vote.getList())
                .execute(this.config.getRewards());
    }
}
