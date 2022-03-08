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

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.validator.annotation.Pattern;
import eu.okaeri.validator.annotation.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenVoteWebhook extends OkaeriConfig {
    @Size(min = 1) private String url;
    @Pattern("GET|POST") private String method = "GET";
    private String content = "";
}
