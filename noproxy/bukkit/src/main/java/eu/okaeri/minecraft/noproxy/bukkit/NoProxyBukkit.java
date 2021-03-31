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

import eu.okaeri.minecraft.noproxy.shared.NoProxyService;

public class NoProxyBukkit extends NoProxyService {

    private final NoProxyBukkitPlugin plugin;

    public NoProxyBukkit(NoProxyBukkitPlugin plugin) {
        super(plugin.getClient());
        this.plugin = plugin;
    }

    @Override
    public void warning(String message) {
        this.plugin.getLogger().warning(message);
    }

    @Override
    public void info(String message) {
        this.plugin.getLogger().info(message);
    }

    @Override
    public void dispatchAsync(Runnable runnable) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }
}
