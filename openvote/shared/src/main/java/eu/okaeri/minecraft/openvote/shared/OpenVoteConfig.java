/*
 * OK! OpenVote Minecraft
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
package eu.okaeri.minecraft.openvote.shared;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.serdes.commons.duration.DurationSpec;
import eu.okaeri.platform.core.annotation.Configuration;
import eu.okaeri.validator.annotation.Min;
import eu.okaeri.validator.annotation.Pattern;
import eu.okaeri.validator.annotation.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Configuration
@Header("##################################################################")
@Header("#                                                                #")
@Header("#    OK! OpenVote Minecraft                                      #")
@Header("#                                                                #")
@Header("#    Nie wiesz jak skonfigurować? Zerknij do dokumentacji!       #")
@Header("#    https://wiki.okaeri.cloud/pl/uslugi/openvote/minecraft      #")
@Header("#                                                                #")
@Header("#    Trouble configuring? Check out the documentation!           #")
@Header("#    https://wiki.okaeri.cloud/en/services/openvote/minecraft    #")
@Header("#                                                                #")
@Header("##################################################################")
public class OpenVoteConfig extends OkaeriConfig {

    @Exclude
    public static final String PLACEHOLDER_SERVER = "example.com";

    @Size(min = 4, max = 64)
    @Pattern(value = "(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]", message = "value must be a valid domain")
    @Comment("IDENTYFIKATOR SERWERA")
    @Comment("Wprowadź swój adres serwera, który dodajesz do list.")
    @Comment("Adres musi być domeną i nie może zawierać portu.")
    @Comment(" ")
    @Comment("SERVER IDENTIFIER")
    @Comment("Specify your server address used for server lists.")
    @Comment("The address must be a domain without a port.")
    private String server = PLACEHOLDER_SERVER;

    @Comment("KOMENDY NAGRODY")
    @Comment("Zmienne:")
    @Comment("   {name} - nick gracza")
    @Comment("   {uuid} - uuid gracza")
    @Comment("   {list} - nazwa listy")
    @Comment("Wprowadź listę komend (bez / na początku), które będą wykonane po oddaniu głosu.")
    @Comment(" ")
    @Comment("REWARD COMMANDS")
    @Comment("Variables:")
    @Comment("   {name} - player nickname")
    @Comment("   {uuid} - player uuid")
    @Comment("   {list} - list name")
    @Comment("Enter commands list (without / at the beginning), which would be executed after a successful vote.")
    private List<String> rewards = Collections.singletonList("give {name} diamond 1");

    @Comment("ADRESY LIST")
    @Comment("Sprawdź na stronie wybranej listy, czy wspiera ona \"OK! OpenVote\"")
    @Comment("i zalecany adres do wprowadzenia. Nie dodawaj na początku \"https://\".")
    @Comment(" ")
    @Comment("LIST ADDRESSES")
    @Comment("Check on the page of the selected list if it supports \"OK! OpenVote\"")
    @Comment("and the recommended address. Do not include \"https://\".")
    private List<String> lists = Collections.singletonList("www.topkamc.pl");

    public Map<String, String> getListsMap() {
        return this.lists.stream().collect(Collectors.toMap(
            name -> name.split("\\+", 2)[0],
            name -> name,
            (u, v) -> {
                throw new IllegalStateException("Duplicate list alias: " + u);
            },
            LinkedHashMap::new
        ));
    }

    @Min(0)
    @DurationSpec(fallbackUnit = ChronoUnit.HOURS)
    @Comment("JAK CZĘSTO GRACZ MOŻE ODBIERAĆ NAGRODĘ ZA GŁOSOWANIE NA POJEDYNCZEJ LIŚCIE?")
    @Comment("Od 1h do 30d.")
    @Comment(" ")
    @Comment("HOW OFTEN IS THE PLAYER ALLOWED TO VOTE ON A SINGLE LIST?")
    @Comment("From 1h up to 30d.")
    private Duration singleCooldown = Duration.ofHours(24);

    @Min(0)
    @Comment("JAK CZĘSTO GRACZ MOZE ODBIERAĆ NAGRODĘ OGÓŁEM?")
    @Comment("Maksymalnie 30d. (0 = bez limitu)")
    @Comment(" ")
    @Comment("HOW OFTEN IS THE PLAYER ALLOWED TO VOTE ON ANY LIST?")
    @Comment("Up to 30d. (0 = no limit)")
    private Duration generalCooldown = Duration.ofSeconds(1);

//    @Pattern("DE|EN|EO|ES|FR|JP|PL|PT|RU")
//    @Comment("JEZYK STRONY GLOSOWANIA")
//    @Comment("Wartoscci: DE, EN, EO, ES, FR, JP, PL, PT, RU")
//    @Comment(" ")
//    @Comment("LANGUAGE OF A VOTING PAGE")
//    @Comment("Values: DE, EN, EO, ES, FR, JP, PL, PT, RU")
//    private String lang = "PL";

    @Comment("UDOSTEPNIANIE NICKU GRACZA")
    @Comment("Czy wtyczka ma wysyłać do listy nick oraz uuid gracza?")
    @Comment("Jeśli wyłączysz tę opcję, może być wymagane od gracza ręczne wpisanie nazwy.")
    @Comment("Do listy zostanie wysłany tylko hash tych wartości (tzw. skrót nieodwracalny).")
    @Comment(" ")
    @Comment("PASSING PLAYER IDENTIFIERS")
    @Comment("Should plugin pass the player nickname/uuid to the list?")
    @Comment("When disabled, list may ask the player to enter their name manually.")
    @Comment("List always receives hash of these values (one-way function result).")
    private boolean passIdentifiers = true;

    @Size(max = 255)
    @Comment("TLO STRONY GLOSOWANIA")
    @Comment("Wspierane przez strony OpenVote. Opcjonalne wsparcie przez właścicieli list.")
    @Comment("Podaj adres URL do obrazka lub kolor w formacie HTML (np. #fff - biały).")
    @Comment("Pozostaw puste, aby nie używać wlasnego tła glosowania.")
    @Comment(" ")
    @Comment("VOTING BACKGROUND")
    @Comment("Supported by OpenVote pages. Optional support on specific lists.")
    @Comment("Specify image URL or HTML color code (e.g. #fff - white).")
    @Comment("Leave empty, to disable feature.")
    private String background = "";

    @Comment("CZY WEBHOOKI MAJĄ BYC WŁĄCZONE?")
    @Comment("Wymagana konfiguracja w sekcji webhooks.")
    @Comment(" ")
    @Comment("SHALL WEBHOOKS BE ENABLED?")
    @Comment("Configuration required. See webhooks section.")
    private boolean enableWebhooks = false;

    @Comment("LISTA WEBHOOKÓW")
    @Comment("Zmienne:")
    @Comment("   {player} - nick gracza")
    @Comment("   {list} - nazwa listy")
    @Comment(" ")
    @Comment("WEBHOOKS LIST")
    @Comment("Variables:")
    @Comment("   {player} - player nickname")
    @Comment("   {list} - list name")
    private List<OpenVoteWebhook> webhooks = new ArrayList<>(); {
        // polish
        OpenVoteWebhook webhookPolish = new OpenVoteWebhook();
        webhookPolish.setUrl("https://discord.com/api/webhooks/x/y");
        webhookPolish.setMethod("POST");
        webhookPolish.setContent("{\n" +
                "  \"content\": null,\n" +
                "  \"embeds\": [\n" +
                "    {\n" +
                "      \"title\": \"Nowy głos!\",\n" +
                "      \"description\": \"Gracz {player} zagłosował na liście {list}.\",\n" +
                "      \"color\": 2067691,\n" +
                "      \"footer\": {\n" +
                "        \"text\": \"OpenVote\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        this.webhooks.add(webhookPolish);
        // english
        OpenVoteWebhook webhookEnglish = new OpenVoteWebhook();
        webhookEnglish.setUrl("https://discord.com/api/webhooks/x/y");
        webhookEnglish.setMethod("POST");
        webhookEnglish.setContent("{\n" +
                "  \"content\": null,\n" +
                "  \"embeds\": [\n" +
                "    {\n" +
                "      \"title\": \"New vote!\",\n" +
                "      \"description\": \"Player {player} voted on {list}.\",\n" +
                "      \"color\": 2067691,\n" +
                "      \"footer\": {\n" +
                "        \"text\": \"OpenVote\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        this.webhooks.add(webhookEnglish);
    }

    @Comment("ID STATYSTYK")
    @Comment("Unikalne id wygenerowane dla tej instalacji wtyczki.")
    @Comment(" ")
    @Comment("Jeśli masz sieć serwerów, możesz ustawić takie")
    @Comment("samo na każdym trybie, aby uniemożliwić zbieranie")
    @Comment("nagrody na każdym z trybów oddzielnie.")
    @Comment(" ")
    @Comment("Nie udostępniaj nikomu tej wartości, to ona gwarantuje bezpieczeństwo systemu.")
    @Comment("W przypadku, gdy ktoś ją pozna, będzie w stanie potwierdzić głos bez")
    @Comment("faktycznego dokonania głosowania, a nawet bez wchodzenia na listę.")
    @Comment(" ")
    @Comment("Jeśli dojdzie do ujawnienia tego ciągu, jak najszybciej dokonaj zmiany na")
    @Comment("nowe losowe UUID, możesz dokonać tego używając komendy \"/openvote reset\".")
    @Comment(" ")
    @Comment(" ")
    @Comment("STATS ID")
    @Comment("Unique id generated just for this installation.")
    @Comment(" ")
    @Comment("If plugin is used on the network with multiple servers")
    @Comment("you can set the same value on all your gamemodes to prevent")
    @Comment("players from receiving rewards on multiple gamemodes.")
    @Comment(" ")
    @Comment("Do not share this value with anyone. It guards security of the system.")
    @Comment("If someone was able to access this token, an unauthorized")
    @Comment("confirmation of vote might be possible, even without visiting the list.")
    @Comment(" ")
    @Comment("In case of a leak or leak suspicion, change the value ASAP")
    @Comment("for the new random UUID, you can use a build-in \"/openvote reset\".")
    private UUID statsId = UUID.randomUUID();
}
