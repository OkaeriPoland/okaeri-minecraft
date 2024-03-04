/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2023 Okaeri, Dawid Sawicki
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
package eu.okaeri.minecraft.aicensor.shared.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.Variable;
import eu.okaeri.platform.core.annotation.Configuration;
import eu.okaeri.sdk.aicensor.AiCensorClient;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.regex.Pattern;

@Getter
@Configuration
@Header("##################################################################")
@Header("#                                                                #")
@Header("#    OK! AI.Censor Minecraft                                     #")
@Header("#                                                                #")
@Header("#    Nie wiesz jak skonfigurować? Zerknij do dokumentacji!       #")
@Header("#    https://wiki.okaeri.cloud/pl/uslugi/aicensor/minecraft      #")
@Header("#                                                                #")
@Header("#    Trouble configuring? Check out the documentation!           #")
@Header("#    https://wiki.okaeri.cloud/en/services/aicensor/minecraft    #")
@Header("#                                                                #")
@Header("##################################################################")
public class AiCensorConfig extends OkaeriConfig {

    @Comment("")
    @Comment("USTAWIENIA POŁĄCZENIA")
    @Comment("CONNECTION SETTINGS")
    private Client client = new Client();

    @Getter
    public static class Client extends OkaeriConfig {

        public static final transient String PLACEHOLDER_SECRET = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";

        @Variable("AICENSOR_SECRET")
        @Comment("Twój klucz prywatny dostępu do API dostępny w panelu")
        @Comment("Your API access private key available in the admin console")
        private String secret = PLACEHOLDER_SECRET;

        @Comment("Tryb analizy wiadomości (zmienianie zalecane jedynie w przypadku problemów)")
        @Comment("- strict : łączenie podzielonych słów (wykrywa np. 'eh, jĘb... acĆ was')")
        @Comment("- subst : analiza zastąpień (wykrywa np. 'eh, j3b@cĆ was')")
        @Comment("- unbound : brak wymogu odrębności słowa (wykrywa np. 'ehjĘbacĆwas')")
        @Comment(" ")
        @Comment("Message analysis mode (changing recommended only in case of problems)")
        @Comment("- strict : combining broken words (detects e.g. 'eh, jĘb... acĆ was')")
        @Comment("- subst : substitution analysis (detects e.g. 'eh, j3b@cĆ was')")
        @Comment("- unbound : removed requirement for word separation (detects e.g. 'ehjĘbacĆwas')")
        private String mode = "strict+subst";

        @Comment("Zachowanie podczas niedostępności API (np. błąd połączenia, osiągnięcie limitu)")
        @Comment("- SILENT : traktowanie każdej akcji jako niewulgarnej bez żadnych informacji")
        @Comment("- WARN : traktowanie każdej akcji jako niewulgarnej z informacją w logach")
        @Comment("- PREVENT : zablokowanie weryfikowanej akcji całkowicie, bez kary (niezalecane)")
        @Comment(" ")
        @Comment("Behavior during API unavailability (e.g. connection error, limit reached)")
        @Comment("- SILENT : treating each action as non-vulgar without any information")
        @Comment("- WARN : treating each action as non-vulgar with information in the logs")
        @Comment("- PREVENT : blocking the action completely, without penalty (not recommended)")
        private AiCensorFailMode fail = AiCensorFailMode.WARN;

        @Comment("Pozwala ustawić alternatywny adres serwera API w celu testowania wczesnych wersji")
        @Comment("Allows you to set an alternate API server address to test early releases")
        private String api = "default";

        public AiCensorClient create() {
            if ("default".equalsIgnoreCase(this.getApi())) {
                return new AiCensorClient(this.getSecret());
            }
            return new AiCensorClient(this.getApi(), this.getSecret());
        }

        public boolean isConfigured() {
            return !PLACEHOLDER_SECRET.equals(this.getSecret());
        }
    }

    @Comment("")
    @Comment("USTAWIENIA WYKRYWANIA")
    @Comment("DETECTION SETTINGS")
    private Check check = new Check();

    @Getter
    public static class Check extends OkaeriConfig {

        @Comment("czat - Sprawdzanie asynchroniczne całkowicie blokujące wiadomość przy wykryciu")
        @Comment("chat - Asynchronous check completely blocking message on detection")
        private Chat chat = new Chat();

        @Getter
        public static class Chat extends OkaeriConfig {
            private boolean enabled = true;
        }

        @Comment("komendy - Sprawdzanie asynchroniczne blokujące komendę przy wykryciu (może psuć wtyczki na wyciszenia i podobne)")
        @Comment("commands - Asynchronous check blocking command on detection (may break mute punishments plugins and similar)")
        private Command command = new Command();

        @Getter
        public static class Command extends OkaeriConfig {
            private boolean enabled = true;
            @Comment("Lista wyrażeń regularnych sprawdzanych komend (muszą zawierać grupę 'content' która jest jedynym sprawdzanym fragmentem)")
            @Comment("List of regular expressions matching checked commands (must contain 'content' group which is the only fragment checked)")
            private List<Pattern> targets = Arrays.asList(
                Pattern.compile("/(?:w|m|t|pm|msg|emsg|epm|tell|etell|whisper|ewhisper) (?:[^ ]+) (?<content>.+)"),
                Pattern.compile("/(?:r|er|reply|ereply) (?<content>.+)"),
                Pattern.compile("/say (?<content>.+)")
            );
        }

        @Comment("książki - Sprawdzanie asynchroniczne ustawiające treść książki na poprzednią przy wykryciu")
        @Comment("books - Asynchronous check that reverts the content of the book to the previous one on detection")
        private Book book = new Book();

        @Getter
        public static class Book extends OkaeriConfig {
            private boolean enabled = true;
        }

        @Comment("znaki - Sprawdzanie asynchroniczne ustawiające treść znaku na poprzednią przy wykryciu")
        @Comment("signs - Asynchronous check that reverts the content of a character to the previous one on detection")
        private Sign sign = new Sign();

        @Getter
        public static class Sign extends OkaeriConfig {
            private boolean enabled = true;
        }

        @Comment("nicki - Sprawdzanie asynchroniczne blokujące wejście na serwer z wulgarnym nickiem")
        @Comment("- CLEAR : wymuszony tryb unbound, wymaga zero wykryć w każdej kategorii")
        @Comment("- VULGAR : wymuszony tryb unbound, ignoruje kategorię pogard i bluzg (zalecane)")
        @Comment("- OFF : wyłączone")
        private Nickname nickname = new Nickname();

        @Getter
        public static class Nickname extends OkaeriConfig {
            private AiCensorCheckNicknameMode enabled = AiCensorCheckNicknameMode.OFF;
        }
    }

    @Comment("")
    @Comment("ZACHOWANIE DLA WYKRYĆ")
    @Comment(" ")
    @Comment("Porada - w przypadku chęci usunięcia kar w danej kategorii, zamiast używać IGNORE, można")
    @Comment("  ustawić 'commands: []', dzięki czemu wiadomości nadal będą blokowane, lecz bez kar.")
    @Comment(" ")
    @Comment("commands: Komendy obejmujące jedynie wykrycia na czacie")
    @Comment("- Liczba w kluczu oznacza które z kolei wykroczenie.")
    @Comment("- Licznik restartuje się wraz z restartem serwera.")
    @Comment(" ")
    @Comment("type: Akcja wykrycia")
    @Comment("- BLOCK : zablokuj wiadomość")
    @Comment("- IGNORE : nie rób nic")
    @Comment(" ")
    @Comment(" ")
    @Comment("BEHAVIOR FOR DETECTIONS")
    @Comment(" ")
    @Comment("Tip - if you want to remove penalties in a category, instead of using IGNORE, you can ")
    @Comment("  set 'commands: []', so messages will still be blocked, but without penalties.")
    @Comment(" ")
    @Comment("commands: Commands for chat detections only")
    @Comment("- The number in the key indicates which offense in order.")
    @Comment("- The counter restarts when the server restarts.")
    @Comment(" ")
    @Comment("type: Detection action")
    @Comment("- BLOCK : block message")
    @Comment("- IGNORE : ignore message")
    private Actions actions = new Actions();

    @Getter
    public static class Actions extends OkaeriConfig {

        private AiCensorAction disdain = new AiCensorAction(
            mapOf(
                "1", "",
                "2", "tempmute {player} 10m obrażanie",
                "default", "tempmute {player} 20m obrażanie"
            ),
            AiCensorActionType.BLOCK
        );

        private AiCensorAction profane = new AiCensorAction(
            mapOf(
                "1", "",
                "2", "tempmute {player} 10m bluzgi",
                "default", "tempmute {player} 20m bluzgi"
            ),
            AiCensorActionType.BLOCK
        );

        private AiCensorAction vulgar = new AiCensorAction(
            mapOf(
                "1", "tempmute {player} 30m wulgaryzmy",
                "2", "tempmute {player} 60m wulgaryzmy",
                "3", "tempmute {player} 2h wulgaryzmy",
                "default", "tempmute {player} 6h wulgaryzmy"
            ),
            AiCensorActionType.BLOCK
        );

        private static Map<String, String> mapOf(@NonNull String... elements) {
            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < elements.length; i += 2) {
                map.put(elements[i], elements[i + 1]);
            }
            return map;
        }
    }

    @Comment("")
    @Comment("DODATKOWE ZASADY WYKRYĆ")
    @Comment(" ")
    @Comment("Porada - możesz sprawdzić listę zasad dla wiadomości komendą /aicensor check <wiadomość...>")
    @Comment("  W wiadomości jest wulgarne słowo i nie pojawia się dla niego zasada? Zgłoś to nam!")
    @Comment(" ")
    @Comment(" ")
    @Comment("ADDITIONAL DETECTION RULES")
    @Comment(" ")
    @Comment("Tip - you can check the list of rules for message with /aicensor check <message...>")
    @Comment("  There is a vulgar word in the message and there is no rule for it? Report it to us!")
    private Rules rules = new Rules();

    @Getter
    public static class Rules extends OkaeriConfig {

        @Comment("Zasady, które zostaną zignorowane przy podejmowaniu decyzji o zablokowaniu wiadomości.")
        @Comment("Przykładowe zasady: 'syf' (bluzga), 'rzygi' (bluzga)")
        @Comment(" ")
        @Comment("Rules that will be ignored when determining should message be blocked.")
        @Comment("Example rules: 'syf' (profane), 'rzygi' (profane)")
        private Set<String> ignored = new HashSet<>(Collections.singletonList("example"));

        @Comment("Zasady, które spowodują zablokowanie wiadomości, nawet gdy ich typ nie jest normalnie blokowany.")
        @Comment("Przykładowe zasady: 'twoja stara' (pogarda), 'pieprzyć głupoty' (bluzga)")
        @Comment(" ")
        @Comment("Rules that will block messages even if their type is not normally blocked (type: IGNORE).")
        @Comment("Example rules: 'twoja stara' (disdain), 'pieprzyć głupoty' (profane)")
        private Set<String> forced = new HashSet<>(Collections.singletonList("przykład"));
    }

    private List<AiCensorWebhook> webhooks = Collections.emptyList();
}
