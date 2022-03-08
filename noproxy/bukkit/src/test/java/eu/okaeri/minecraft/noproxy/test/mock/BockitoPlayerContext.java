package eu.okaeri.minecraft.noproxy.test.mock;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.OfflinePlayerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import lombok.AllArgsConstructor;
import org.awaitility.Awaitility;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AllArgsConstructor
public class BockitoPlayerContext {

    public static BockitoPlayerContext of(Plugin plugin) {
        ServerMock server = (ServerMock) plugin.getServer();
        PlayerMock player = BockitoPlayerMock.of(server, String.valueOf(UUID.randomUUID()).split("-")[4]);
        return new BockitoPlayerContext(plugin, server, player);
    }

    private final Plugin plugin;
    private final ServerMock server;
    private PlayerMock player;

    public BockitoPlayerContext withPermission(String perm, boolean value) {
        Bockito.ensurePermission(perm);
        this.player.addAttachment(this.plugin, perm, true);
        return this;
    }

    public BockitoPlayerContext withPermission(String perm) {
        return this.withPermission(perm, true);
    }

    public BockitoPlayerContext performCommand(String command) {
        this.player.performCommand(command);
        return this;
    }

    public BockitoPlayerContext join() {
        OfflinePlayerMock mock = new OfflinePlayerMock(this.player.getName());
        this.player = mock.join(this.server);
        return this;
    }

    public BockitoPlayerContext awaitMessage(Duration duration, String message) {
        Awaitility.await()
                .atMost(duration)
                .until(() -> {
                    String nextMessage = this.player.nextMessage();
                    if (nextMessage == null) {
                        return false;
                    }
                    assertEquals(message, nextMessage);
                    return true;
                });
        return this;
    }

    public BockitoPlayerContext awaitMessage(String message) {
        return this.awaitMessage(Duration.ofSeconds(1), message);
    }
}
