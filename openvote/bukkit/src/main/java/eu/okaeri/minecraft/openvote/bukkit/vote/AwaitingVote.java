package eu.okaeri.minecraft.openvote.bukkit.vote;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AwaitingVote {
    private UUID id;
    private UUID player;
}
