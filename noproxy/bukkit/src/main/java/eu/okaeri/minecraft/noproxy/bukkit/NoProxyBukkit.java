package eu.okaeri.minecraft.noproxy.bukkit;

import eu.okaeri.minecraft.noproxy.shared.NoProxyDetector;

public class NoProxyBukkit extends NoProxyDetector {

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