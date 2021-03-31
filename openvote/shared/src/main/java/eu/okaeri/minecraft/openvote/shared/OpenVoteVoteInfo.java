package eu.okaeri.minecraft.openvote.shared;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenVoteVoteInfo {
    private String list;
    private String player;
}
