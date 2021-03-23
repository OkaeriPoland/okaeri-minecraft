package eu.okaeri.minecraft.noproxy.shared;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class NoProxyMessages extends OkaeriConfig {
    private String prefix = "&f&lNo.Proxy&7: ";
    private String playerInfo =
            "{PREFIX}&cKorzystanie z serwerow VPN lub proxy jest zabronione na tym serwerze.\n" +
                    "&cJesli uwazasz to za blad, skontaktuj sie z administracja.\n" +
                    "&f\n" +
                    "&cPomoc: &ehttps://example.com/forum";
}
