package eu.okaeri.minecraft.noproxy.test.mock;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BockitoPlayerMock extends PlayerMock {

    public static BockitoPlayerMock of(ServerMock server, String name) {
        BockitoPlayerMock player = new BockitoPlayerMock(server, name);
        server.addPlayer(player);
        return player;
    }

    public BockitoPlayerMock(ServerMock server, String name) {
        super(server, name);
    }

    public BockitoPlayerMock(ServerMock server, String name, UUID uuid) {
        super(server, name, uuid);
    }

    @Override
    public @NotNull String getLocale() {
        return "en_GB";
    }
}
