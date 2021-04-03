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
package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.noproxy.shared.NoProxyConfig;
import eu.okaeri.minecraft.noproxy.shared.NoProxyMessages;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.WithBean;
import eu.okaeri.sdk.noproxy.NoProxyClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
@WithBean(NoProxyConfig.class) // load config
@WithBean(NoProxyConfigurer.class) // check config and create client/noproxy
@WithBean(NoProxyMessages.class) // load messages
@WithBean(NoProxyMessager.class) // create messager helper
@WithBean(NoProxyCommand.class) // register admin command
@WithBean(NoProxyListener.class) // register listener
public class NoProxyBukkitPlugin extends OkaeriBukkitPlugin {
    // expose as api
    @Inject private NoProxyClient client;
    @Inject private NoProxyBukkit noproxy;
}
