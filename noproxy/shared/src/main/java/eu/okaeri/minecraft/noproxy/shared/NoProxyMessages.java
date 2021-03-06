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
package eu.okaeri.minecraft.noproxy.shared;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.platform.core.annotation.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Configuration(path = "messages.yml")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class NoProxyMessages extends OkaeriConfig {
    private String prefix = "&f&lNo.Proxy&7: ";
    private String playerInfo =
            "{PREFIX}&cKorzystanie z serwerow VPN lub proxy jest zabronione na tym serwerze.\n" +
                    "&cJesli uwazasz to za blad, skontaktuj sie z administracja.\n" +
                    "&f\n" +
                    "&cPomoc: &ehttps://example.com/forum";
}
