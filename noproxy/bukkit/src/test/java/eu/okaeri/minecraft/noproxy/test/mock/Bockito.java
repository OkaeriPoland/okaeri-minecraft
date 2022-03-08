package eu.okaeri.minecraft.noproxy.test.mock;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class Bockito {

    private static Plugin plugin;

    public static void setup(Plugin plugin) {
        Bockito.plugin = plugin;
    }

    public static BockitoPlayerContext player() {
        return BockitoPlayerContext.of(plugin);
    }

    public static void ensurePermission(String perm) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (pluginManager.getPermission(perm) != null) {
            return;
        }
        Permission permission = new Permission(perm, PermissionDefault.FALSE);
        pluginManager.addPermission(permission);
    }
}
