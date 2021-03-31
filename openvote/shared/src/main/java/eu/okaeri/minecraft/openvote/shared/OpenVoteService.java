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

import eu.okaeri.sdk.openvote.OpenVoteClient;
import eu.okaeri.sdk.unirest.HttpResponse;
import eu.okaeri.sdk.unirest.Unirest;
import eu.okaeri.sdk.unirest.UnirestInstance;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class OpenVoteService {

    private static final UnirestInstance UNIREST_INSTANCE = new UnirestInstance(Unirest.config()
            .addDefaultHeader("User-Agent", "okaeri-minecraft-openvote/2.0")
            .followRedirects(false)
            .enableCookieManagement(false)
            .connectTimeout(5000)
            .socketTimeout(5000));

    @Getter private final OpenVoteClient client;
    @Getter private final Set<OpenVoteWebhook> webhooks = new HashSet<>();
    private final boolean debug = Boolean.getBoolean("openvoteDebug");

    public OpenVoteService(OpenVoteClient client) {
        this.client = client;
    }

    public void addWebhook(OpenVoteWebhook webhook) {
        if (webhook == null) throw new IllegalArgumentException("webhook cannot be null");
        if (webhook.getUrl() == null) throw new IllegalArgumentException("webhook.url cannot be null");
        if (webhook.getMethod() == null) throw new IllegalArgumentException("webhook.method cannot be null");
        if (!"POST".equals(webhook.getMethod()) && !"GET".equals(webhook.getMethod())) throw new IllegalArgumentException("webhook.method is not POST or GET");
        this.webhooks.add(webhook);
    }

    public void dispatchWebhooks(OpenVoteVoteInfo info, Map<String, String> additionalVariables) {
        if (this.debug) this.info("Checking for webhooks (" + this.webhooks.size() + ")");
        for (OpenVoteWebhook webhook : this.webhooks) {
            if (this.debug) this.info("Dispatching: " + webhook);
            this.dispatchWebhook(info, webhook, additionalVariables);
        }
    }

    public void dispatchWebhook(OpenVoteVoteInfo info, OpenVoteWebhook webhook, Map<String, String> additionalVariables) {
        if (this.debug) this.info("Preparing webhook for " + webhook);
        if (webhook.getUrl() == null) throw new IllegalArgumentException("webhook.url cannot be null");
        String method = (webhook.getMethod() == null) ? "GET" : webhook.getMethod();
        String url = this.replaceVariables(webhook.getUrl(), true, false, this.addressInfoToMap(info), additionalVariables);
        String body = (webhook.getContent() == null) ? "" : webhook.getContent();
        body = this.replaceVariables(body, false, true, this.addressInfoToMap(info), additionalVariables);
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

    private Map<String, String> addressInfoToMap(OpenVoteVoteInfo info) {
        Map<String, String> map = new HashMap<>();
        map.put("player", info.getPlayer());
        map.put("list", info.getList());
        return map;
    }

    @SafeVarargs
    private final String replaceVariables(String text, boolean urlEncode, boolean quotesEscape, Map<String, String>... variableSets) {
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

    public abstract void warning(String message);

    public abstract void info(String message);

    public abstract void dispatchAsync(Runnable runnable);
}
