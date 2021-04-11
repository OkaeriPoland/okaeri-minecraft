package eu.okaeri.minecraft.openvote.bukkit.vote;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.annotation.Timer;
import eu.okaeri.platform.bukkit.commons.time.MinecraftTimeEquivalent;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.sdk.openvote.OpenVoteClient;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVoteCheckRequest;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVoteCheckResult;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVoteState;
import org.bukkit.Server;

import java.util.*;
import java.util.stream.Collectors;

@Timer(rate = MinecraftTimeEquivalent.SECONDS_30, async = true)
public class VotesUpdateTask implements Runnable {

    @Inject private OpenVoteClient client;
    @Inject private OpenVoteConfig config;
    @Inject private OpenVoteMessages messages;
    @Inject private Server server;

    @Inject("awaitingVotes")
    private Set<AwaitingVote> awaitingVotes;

    @Override
    public void run() {

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
        // TODO
    }

    @Bean("awaitingVotes")
    private Set<AwaitingVote> createAwaitingVotes() {
        return new HashSet<>();
    }
}
