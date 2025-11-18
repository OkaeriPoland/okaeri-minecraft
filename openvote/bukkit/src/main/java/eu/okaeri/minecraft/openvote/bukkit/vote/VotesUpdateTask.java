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

import eu.okaeri.commons.bukkit.command.CommandRunner;
import eu.okaeri.commons.bukkit.time.MinecraftTimeEquivalent;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.annotation.Scheduled;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Scheduled(rate = MinecraftTimeEquivalent.SECOND * 30, async = true)
public class VotesUpdateTask implements Runnable {

    private @Inject OpenVoteClient client;
    private @Inject OpenVoteConfig config;
    private @Inject OpenVoteMessages messages;
    private @Inject Plugin plugin;
    private @Inject Server server;
    private @Inject Logger logger;

    @Inject("awaitingVotes")
    private Set<AwaitingVote> awaitingVotes;

    @Override
    public void run() {

        if (this.awaitingVotes.isEmpty()) {
            if (this.config.isDebug()) {
                this.logger.info("No awaiting votes to check");
            }
            return;
        }

        List<UUID> votesIds = this.awaitingVotes.stream()
                .map(AwaitingVote::getId)
                .collect(Collectors.toList());

        if (this.config.isDebug()) {
            this.logger.info("Checking " + votesIds.size() + " vote(s): " + votesIds);
        }

        OpenVoteServerVoteCheckRequest request = new OpenVoteServerVoteCheckRequest(votesIds);
        OpenVoteServerVoteCheckResult checkResult = this.client.postServerVoteCheck(request);

        if (this.config.isDebug()) {
            this.logger.info("Received check result with " + checkResult.getVotes().size() + " vote status update(s)");
        }

        for (Map.Entry<UUID, String> voteEntry : checkResult.getVotes().entrySet()) {

            UUID voteId = voteEntry.getKey();
            String status = voteEntry.getValue();

            if (this.config.isDebug()) {
                this.logger.info("Processing vote " + voteId + " with status: " + status);
            }

            if (OpenVoteServerVoteState.WAIT.name().equals(status)) {
                continue;
            }

            if (OpenVoteServerVoteState.REMOVE.name().equals(status)) {
                this.awaitingVotes.removeIf(vote -> vote.getId().equals(voteId));
                if (this.config.isDebug()) {
                    this.logger.info("Removed vote " + voteId + " from awaiting queue");
                }
                continue;
            }

            if (OpenVoteServerVoteState.REWARD.name().equals(status)) {
                this.awaitingVotes.stream()
                        .filter(vote -> vote.getId().equals(voteId))
                        .findAny()
                        .ifPresent(this::reward);
            }
        }
    }

    private void reward(AwaitingVote vote) {

        if (this.config.isDebug()) {
            this.logger.info("Attempting to reward vote " + vote.getId() + " for player " + vote.getPlayer() + " from list " + vote.getList());
        }

        Player player = this.server.getPlayer(vote.getPlayer());
        if ((player == null) || !player.isOnline()) {
            if (this.config.isDebug()) {
                this.logger.info("Player " + vote.getPlayer() + " is not online, skipping reward");
            }
            return;
        }

        this.awaitingVotes.remove(vote);
        if (this.config.isDebug()) {
            this.logger.info("Executing " + this.config.getRewards().size() + " reward command(s) for player " + player.getName());
        }

        CommandRunner.of(this.plugin, player)
                .field("name", player.getName())
                .field("uuid", String.valueOf(player.getUniqueId()))
                .field("list", vote.getList())
                .execute(this.config.getRewards());
    }
}
