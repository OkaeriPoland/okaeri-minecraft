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
package eu.okaeri.minecraft.aicensor.shared;

import eu.okaeri.minecraft.aicensor.shared.config.*;
import eu.okaeri.sdk.aicensor.AiCensorClient;
import eu.okaeri.sdk.aicensor.error.AiCensorException;
import eu.okaeri.sdk.aicensor.model.*;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AiCensorService {

    private static final UnirestInstance UNIREST_INSTANCE = new UnirestInstance(Unirest.config()
        .addDefaultHeader("User-Agent", "okaeri-minecraft-aicensor/2.0")
        .followRedirects(false)
        .enableCookieManagement(false)
        .connectTimeout(5000)
        .socketTimeout(5000));

    protected @Getter @Setter AiCensorConfig config;
    protected @Getter @Setter AiCensorClient client;
    protected final boolean debug = Boolean.getBoolean("aicensorDebug");

    public AiCensorService(@NonNull AiCensorConfig config, @NonNull AiCensorClient client) {
        this.config = config;
        this.client = client;
    }

    public AiCensorResponse shouldBeBlocked(@NonNull String message) {
        return this.shouldBeBlocked(message, this.config.getClient().getMode(), Collections.emptyMap());
    }

    public AiCensorResponse shouldBeBlocked(@NonNull String message, @NonNull Map<String, String> additionalVariables) {
        return this.shouldBeBlocked(message, this.config.getClient().getMode(), additionalVariables);
    }

    public AiCensorResponse shouldBeBlocked(@NonNull String message, @NonNull String modeString, @NonNull Map<String, String> additionalVariables) {

        AiCensorAnalysisSource source;
        try {
            source = this.client.getAnalysis(message, AiCensorAnalysisMode.fromModeString(modeString));
        } catch (AiCensorException exception) {
            AiCensorFailMode failMode = this.config.getClient().getFail();
            if (this.debug || (failMode != AiCensorFailMode.SILENT)) {
                this.warning("Error communicating OK! AI.Censor API: " + exception.getMessage());
            }
            if (this.debug) {
                exception.printStackTrace();
            }
            return new AiCensorResponse(-1, -1, -1);
        }

        long disdain = this.countFragments(source.getFragments(), AiCensorAnalysisFragmentType.DISDAIN, this.config.getActions().getDisdain());
        long profane = this.countFragments(source.getFragments(), AiCensorAnalysisFragmentType.PROFANE, this.config.getActions().getProfane());
        long vulgar = this.countFragments(source.getFragments(), AiCensorAnalysisFragmentType.VULGAR, this.config.getActions().getVulgar());
        boolean block = (disdain + profane + vulgar) > 0;

        AiCensorAnalysisDetails details = new AiCensorAnalysisDetails(
            new AiCensorAnalysisJudgement(!block, (int) disdain, (int) profane, (int) vulgar),
            new AiCensorAnalysisSuggestions(block, block),
            source
        );

        if (block) {
            this.dispatchAsync(() -> this.dispatchWebhooks(details, additionalVariables));
            this.info("Analyzed message '" + message + "' [disdain: " + disdain + ", profane: " + profane + ", vulgar: " + vulgar + "]");
        }

        return new AiCensorResponse(disdain, profane, vulgar);
    }

    protected long countFragments(@NonNull List<AiCensorAnalysisFragment> fragments, @NonNull AiCensorAnalysisFragmentType type, @NonNull AiCensorAction action) {

        Set<String> forcedRules = this.config.getRules().getForced();
        Set<String> ignoredRules = this.config.getRules().getIgnored();

        return fragments.stream()
            .filter(frag -> frag.getType() == type)
            .filter(frag -> !ignoredRules.contains(frag.getRule())) // only count when not ignored
            .filter(frag -> (action.getType() == AiCensorActionType.BLOCK) || forcedRules.contains(frag.getRule())) // only count when action set to BLOCK or forced
            .count();
    }

    public void dispatchWebhooks(@NonNull AiCensorAnalysisDetails details, @NonNull Map<String, String> additionalVariables) {
        if (this.debug) this.info("Checking for webhooks (" + this.config.getWebhooks().size() + ")");
        for (AiCensorWebhook webhook : this.config.getWebhooks()) {
            this.dispatchWebhook(details, webhook, additionalVariables);
        }
    }

    public void dispatchWebhook(@NonNull AiCensorAnalysisDetails details, @NonNull AiCensorWebhook webhook, @NonNull Map<String, String> additionalVariables) {
        if (this.debug) this.info("Preparing webhook for " + webhook);
        if (webhook.getUrl() == null) throw new IllegalArgumentException("webhook.url cannot be null");
        String method = (webhook.getMethod() == null) ? "GET" : webhook.getMethod();
        String url = this.replaceVariables(webhook.getUrl(), true, false, this.addressInfoToMap(details), additionalVariables);
        String body = (webhook.getContent() == null) ? "" : webhook.getContent();
        body = this.replaceVariables(body, false, true, this.addressInfoToMap(details), additionalVariables);
        try {
            if (this.debug) this.info("Sending webhook to '" + url + "'");
            HttpResponse<String> data;
            if ("GET".equals(method)) {
                data = UNIREST_INSTANCE.get(url)
                    .asString();
            } else if ("POST".equals(method)) {
                data = UNIREST_INSTANCE.post(url)
                    .body(body)
                    .asString();
            } else {
                throw new IllegalArgumentException("Tried to dispatch webhook with unknown method: " + method);
            }
            if (this.debug) this.info("Response body '" + url + "': " + (data == null ? "null" : data.getBody()));
        } catch (Exception exception) {
            this.warning("Webhook (" + url + ") failed: " + exception.getMessage());
            if (this.debug) exception.printStackTrace();
        }
    }

    private Map<String, String> addressInfoToMap(@NonNull AiCensorAnalysisDetails details) {
        Map<String, String> map = new HashMap<>();
        map.put("judgement.clear", String.valueOf(details.getJudgement().isClear()));
        map.put("judgement.disdain", String.valueOf(details.getJudgement().getDisdain()));
        map.put("judgement.profane", String.valueOf(details.getJudgement().getProfane()));
        map.put("judgement.vulgar", String.valueOf(details.getJudgement().getVulgar()));
        map.put("suggestions.block", String.valueOf(details.getSuggestions().isBlock()));
        map.put("suggestions.verify", String.valueOf(details.getSuggestions().isVerify()));
        map.put("source.fragments.rules", details.getSource().getFragments().stream()
            .map(AiCensorAnalysisFragment::getRule)
            .collect(Collectors.joining(", ")));
        map.put("source.content", details.getSource().getContent());
        return map;
    }

    @SafeVarargs
    private final String replaceVariables(String text, boolean urlEncode, boolean quotesEscape, @NonNull Map<String, String>... variableSets) {
        for (Map<String, String> variables : variableSets) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (quotesEscape) {
                    value = value.replaceAll("\"", "\\\\\"");
                }
                if (urlEncode) {
                    try {
                        value = URLEncoder.encode(value, "UTF-8");
                    } catch (UnsupportedEncodingException exception) {
                        this.warning("Failed to encode '" + value + "': " + exception.getMessage());
                    }
                }
                text = text.replace("{" + key + "}", value);
            }
        }
        return text;
    }

    public abstract void warning(@NonNull String message);

    public abstract void info(@NonNull String message);

    public abstract void dispatchAsync(@NonNull Runnable runnable);
}
