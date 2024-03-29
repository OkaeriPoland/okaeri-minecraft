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
package eu.okaeri.minecraft.openvote.shared;

import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.core.annotation.Messages;
import lombok.Getter;

@Getter
@Messages
public class OpenVoteMessages extends LocaleConfig {

    private String prefix = "&f&lOpenVote&7: ";

    private String commandsOpenvoteReloadDescription = "reloads the config";
    private String commandsOpenvoteReloadSuccess = "~The configuration reloaded.";
    private String commandsOpenvoteReloadFail = "~Error during reload! More information in the console.";

    private String commandsOpenvoteResetDescription = "resets secret token and statistics";
    private String commandsOpenvoteResetSuccess = "~The plugin's stats id has been reset!";

    private String commandsVoteListDescription = "displays all list available for voting";
    private String commandsVoteListTemplate = "~Use /vote <list> with one of the following:\n{entries}";
    private String commandsVoteListEntry = "- {list}";

    private String commandsVoteVoteDescription = "starts voting process on a specific list";
    private String commandsVoteVoteListInvalid = "~The list {list} is invalid! Check /vote list";
    private String commandsVoteVoteError = "~Error submitting the vote ({error}): {message}";
    private String commandsVoteVoteUrl = "~Continue voting here: {url}";
}
