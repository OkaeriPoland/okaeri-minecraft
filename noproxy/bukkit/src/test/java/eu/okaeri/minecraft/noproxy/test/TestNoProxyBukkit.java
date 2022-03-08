package eu.okaeri.minecraft.noproxy.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import eu.okaeri.minecraft.noproxy.bukkit.NoProxyBukkitPlugin;
import eu.okaeri.minecraft.noproxy.test.mock.Bockito;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static eu.okaeri.minecraft.noproxy.test.mock.Bockito.player;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestNoProxyBukkit {

    @BeforeAll
    public void setup() {

        // setup fake token for tests
        System.setProperty("NOPROXY_TOKEN", String.valueOf(UUID.randomUUID()));

        // load plugin
        ServerMock server = MockBukkit.mock();
        Plugin plugin = MockBukkit.load(NoProxyBukkitPlugin.class);

        // bockito
        Bockito.setup(plugin);
    }

    @Test
    public void test_noproxy_no_permissions() {
        player()
                .performCommand("noproxy").awaitMessage(RED + "No permission noproxy.admin!")
                .performCommand("noproxy reload").awaitMessage(RED + "No permission noproxy.admin!");
    }

    @Test
    public void test_noproxy_reload_success() {
        player()
                .withPermission("noproxy.admin")
                .performCommand("noproxy reload")
                .awaitMessage(GREEN + "The configuration has been reloaded!");
    }

    @AfterAll
    public void teardown() {
        MockBukkit.unmock();
    }
}
