/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2024 Okaeri, Dawid Sawicki
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

import eu.okaeri.minecraft.noproxy.shared.NoProxyService;
import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
import eu.okaeri.sdk.noproxy.NoProxyClient;

public class NoProxyVelocity extends NoProxyService {

    private final OkaeriVelocityPlugin plugin;

    public NoProxyVelocity(OkaeriVelocityPlugin plugin, NoProxyClient client) {
        super(client);
        this.plugin = plugin;
    }

    @Override
    public void warning(String message) {
        this.plugin.getLogger().warn(message);
    }

    @Override
    public void info(String message) {
        this.plugin.getLogger().info(message);
    }

    @Override
    public void dispatchAsync(Runnable runnable) {
        this.plugin.getProxy().getScheduler().buildTask(this.plugin, runnable).schedule();
    }
}
