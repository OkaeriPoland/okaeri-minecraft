package eu.okaeri.minecraft.openvote.bukkit;

import eu.okaeri.minecraft.openvote.shared.OpenVoteService;

public class OpenVoteBukkit extends OpenVoteService {

    private final OpenVoteBukkitPlugin plugin;

    public OpenVoteBukkit(OpenVoteBukkitPlugin plugin) {
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
