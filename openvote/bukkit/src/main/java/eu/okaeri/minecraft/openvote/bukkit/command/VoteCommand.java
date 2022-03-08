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
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Completion;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.bukkit.annotation.Async;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.bukkit.annotation.Sender;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.openvote.bukkit.vote.AwaitingVote;
import eu.okaeri.minecraft.openvote.shared.OpenVoteConfig;
import eu.okaeri.minecraft.openvote.shared.OpenVoteMessages;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import eu.okaeri.platform.bukkit.i18n.PlayerLocaleProvider;
import eu.okaeri.sdk.openvote.OpenVoteClient;
import eu.okaeri.sdk.openvote.error.OpenVoteException;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVote;
import eu.okaeri.sdk.openvote.model.server.OpenVoteServerVoteStartRequest;
import eu.okaeri.sdk.openvote.model.vote.OpenVoteGame;
import eu.okaeri.sdk.openvote.model.vote.OpenVoteIdentifierType;
import eu.okaeri.sdk.openvote.model.vote.OpenVoteLang;
import eu.okaeri.sdk.openvote.model.vote.OpenVoteVoteIdentifier;
import kong.unirest.UnirestException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Async
@Permission("openvote.vote")
@Command(label = "vote", aliases = "glosuj", description = "${commandsVoteListDescription}")
public class VoteCommand implements CommandService {

    private static final PlayerLocaleProvider LOCALE_PROVIDER = new PlayerLocaleProvider();

    @Inject private OpenVoteConfig config;
    @Inject private OpenVoteMessages messages;
    @Inject("awaitingVotes") private Set<AwaitingVote> awaitingVotes;
    @Inject private BI18n i18n;
    @Inject private OpenVoteClient client;

    @Executor(pattern = {"list", "lists"}, description = "${commandsVoteListDescription}")
    public Message lists(CommandSender sender) {
        return this.i18n.get(sender, this.messages.getCommandsVoteListTemplate())
                .with("entries", this.config.getListsMap().keySet().stream()
                        .map(list -> this.i18n.get(sender, this.messages.getCommandsVoteListEntry())
                                .with("list", list)
                                .apply())
                        .collect(Collectors.joining("\n")));
    }

    @Completion(arg = "list", value = "@lists")
    @Executor(description = "${commandsVoteVoteDescription}")
    public Message _vote(@Sender Player player, @Arg String list) {

        list = list.toLowerCase(Locale.ROOT);
        if (!this.config.getListsMap().containsKey(list)) {
            return this.i18n.get(player, this.messages.getCommandsVoteVoteListInvalid())
                    .with("list", list);
        }

        list = this.config.getListsMap().get(list);
        List<OpenVoteVoteIdentifier> identifiers = Arrays.asList(
                new OpenVoteVoteIdentifier(OpenVoteIdentifierType.UUID.name(), String.valueOf(player.getUniqueId())),
                new OpenVoteVoteIdentifier(OpenVoteIdentifierType.USERNAME.name(), player.getName()));

        String background = (this.config.getBackground() == null) ? null
                : (this.config.getBackground().isEmpty() ? null
                : this.config.getBackground());

        OpenVoteServerVoteStartRequest request = new OpenVoteServerVoteStartRequest();
        request.setList(list);
        request.setStatsId(this.config.getStatsId());
        request.setSingleCooldown(this.config.getSingleCooldown());
        request.setGeneralCooldown(this.config.getGeneralCooldown());
        request.setPassIdentifiers(this.config.isPassIdentifiers());
        request.setIdentifiers(identifiers);
        request.setServer(this.config.getServer());
        request.setGame(OpenVoteGame.MINECRAFT_JAVA.name());
        request.setLang(this.getLang(player).name());
        request.setBackground(background);

        try {
            OpenVoteServerVote vote = this.client.postServerVoteNew(request);
            this.awaitingVotes.add(new AwaitingVote(vote.getServerVoteId(), player.getUniqueId(), list));
            return this.i18n.get(player, this.messages.getCommandsVoteVoteUrl())
                    .with("url", vote.getUrl());
        } catch (OpenVoteException exception) {
            return this.i18n.get(player, this.messages.getCommandsVoteVoteError())
                    .with("error", exception.getApiError().getError())
                    .with("message", exception.getApiError().getMessage());
        } catch (UnirestException exception) {
            return this.i18n.get(player, this.messages.getCommandsVoteVoteError())
                    .with("error", exception.getCause().getClass().getSimpleName())
                    .with("message", exception.getCause().getMessage());
        }
    }

    private OpenVoteLang getLang(Player player) {

        Locale locale = LOCALE_PROVIDER.getLocale(player);
        String language = locale.getLanguage();

        if ((language == null) || language.isEmpty()) {
            return OpenVoteLang.EN;
        }

        try {
            return OpenVoteLang.valueOf(language.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return OpenVoteLang.EN;
        }
    }
}
