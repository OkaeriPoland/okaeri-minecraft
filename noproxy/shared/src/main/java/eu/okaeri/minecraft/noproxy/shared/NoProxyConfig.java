/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2021 Okaeri, Dawid Sawicki
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
package eu.okaeri.minecraft.noproxy.shared;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import eu.okaeri.platform.core.annotation.Configuration;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Configuration
@Header("################################################################")
@Header("#                                                              #")
@Header("#    OK! No.Proxy Minecraft                                    #")
@Header("#                                                              #")
@Header("#    Nie wiesz jak skonfigurować? Zerknij do dokumentacji!     #")
@Header("#    https://wiki.okaeri.eu/pl/uslugi/noproxy/minecraft        #")
@Header("#                                                              #")
@Header("#    Trouble configuring? Check out the documentation!         #")
@Header("#    https://wiki.okaeri.eu/en/services/noproxy/minecraft      #")
@Header("#                                                              #")
@Header("################################################################")
public class NoProxyConfig extends OkaeriConfig {

    @Variable("NOPROXY_TOKEN")
    @Comment({"Klucz prywatny API", "API secret"})
    private String token = "";

    @Comment("Biala lista (wpisane nicki lub ip nie beda blokowane)")
    @Comment("Whitelist (nicknames or ips)")
    private List<String> whitelist = Collections.singletonList("127.0.0.1");

    @Comment({"Webhooki", "Webhooks"})
    private List<NoProxyWebhook> webhooks = Collections.emptyList();
}
