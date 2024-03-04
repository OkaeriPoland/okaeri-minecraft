/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2023 Okaeri, Dawid Sawicki
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
package eu.okaeri.minecraft.aicensor.bukkit;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.minecraft.aicensor.shared.AiCensorResponse;
import eu.okaeri.minecraft.aicensor.shared.AiCensorService;
import eu.okaeri.minecraft.aicensor.shared.config.AiCensorConfig;
import eu.okaeri.platform.bukkit.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.sdk.aicensor.AiCensorClient;
import eu.okaeri.sdk.aicensor.model.AiCensorAnalysisFragmentType;
import lombok.NonNull;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class AiCensorBukkit extends AiCensorService {

    private final Map<UUID, Map<AiCensorAnalysisFragmentType, Integer>> offenses = new ConcurrentHashMap<>();

    private @Inject Logger logger;
    private @Inject PlatformScheduler scheduler;
    private @Inject Server server;
    private @Inject JavaPlugin plugin;

    @Inject
    public AiCensorBukkit(@Inject @NonNull AiCensorConfig config, @NonNull AiCensorClient client) {
        super(config, client);
    }

    @Override
    public void warning(@NonNull String message) {
        this.plugin.getLogger().warning(message);
    }

    @Override
    public void info(@NonNull String message) {
        this.plugin.getLogger().info(message);
    }

    @Override
    public void dispatchAsync(@NonNull Runnable runnable) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    public void punish(@NonNull Player player, @NonNull AiCensorResponse response) {

        AiCensorAnalysisFragmentType category = this.resolveCategory(response);
        Map<String, String> commands = this.resolveCommands(response);

        Map<AiCensorAnalysisFragmentType, Integer> categoryOffenses = this.offenses.computeIfAbsent(player.getUniqueId(), playerId -> new ConcurrentHashMap<>());
        int newOffenses = categoryOffenses.compute(category, (current, old) -> (old == null) ? 1 : (old + 1));

        String defaultCommand = commands.get("default");
        if (defaultCommand == null) {
            this.logger.warning("The command for the level 'default' was not set for " + category);
        }

        String punishmentCommand = commands.getOrDefault(String.valueOf(newOffenses), defaultCommand);
        if ((punishmentCommand == null) || punishmentCommand.isEmpty()) {
            return;
        }

        String resultCommand = punishmentCommand.replace("{player}", player.getName());
        this.scheduler.runSync(() -> this.server.dispatchCommand(this.server.getConsoleSender(), resultCommand));
    }

    private AiCensorAnalysisFragmentType resolveCategory(@NonNull AiCensorResponse response) {
        if (response.getVulgar() > 0) return AiCensorAnalysisFragmentType.VULGAR;
        if (response.getProfane() > 0) return AiCensorAnalysisFragmentType.PROFANE;
        if (response.getDisdain() > 0) return AiCensorAnalysisFragmentType.DISDAIN;
        throw new IllegalArgumentException("Cannot resolve category of clear response!");
    }

    private Map<String, String> resolveCommands(@NonNull AiCensorResponse response) {
        if (response.getVulgar() > 0) return this.config.getActions().getVulgar().getCommands();
        if (response.getProfane() > 0) return this.config.getActions().getProfane().getCommands();
        if (response.getDisdain() > 0) return this.config.getActions().getDisdain().getCommands();
        throw new IllegalArgumentException("Cannot resolve commands of clear response!");
    }
}
