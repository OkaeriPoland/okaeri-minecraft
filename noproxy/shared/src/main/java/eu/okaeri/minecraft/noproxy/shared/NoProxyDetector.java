/*
 * OK! No.Proxy Minecraft
 * Copyright (C) 2020 Okaeri, Dawid Sawicki
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

import eu.okaeri.sdk.noproxy.NoProxyClient;
import eu.okaeri.sdk.noproxy.error.NoProxyException;
import eu.okaeri.sdk.noproxy.model.NoProxyAddressInfo;
import eu.okaeri.sdk.noproxy.model.NoProxyError;
import eu.okaeri.sdk.unirest.HttpResponse;
import eu.okaeri.sdk.unirest.Unirest;
import eu.okaeri.sdk.unirest.UnirestInstance;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class NoProxyDetector {

    private static final long DATA_DISCARD_TIME = TimeUnit.SECONDS.toMillis(60);
    private static final UnirestInstance UNIREST_INSTANCE = new UnirestInstance(Unirest.config()
            .addDefaultHeader("User-Agent", "okaeri-minecraft-noproxy/2.0")
            .followRedirects(false)
            .enableCookieManagement(false)
            .connectTimeout(5000)
            .socketTimeout(5000));

    private final NoProxyClient client;
    private final boolean debug = Boolean.getBoolean("noproxyDebug");
    private final boolean webhookAlways = Boolean.getBoolean("noproxyWebhookAlways");

    private final Map<String, NoProxyAddressInfo> infoMap = new ConcurrentHashMap<>();
    private final Map<String, Long> timeMap = new ConcurrentHashMap<>();
    private final Set<NoProxyWebhook> webhookList = new HashSet<>();
    private long nextDiscard = System.currentTimeMillis() + DATA_DISCARD_TIME;

    public NoProxyDetector(NoProxyClient client) {
        this.client = client;
    }

    public NoProxyClient getClient() {
        return this.client;
    }

    public void addWebhook(NoProxyWebhook webhook) {
        if (webhook == null) throw new IllegalArgumentException("webhook cannot be null");
        if (webhook.getUrl() == null) throw new IllegalArgumentException("webhook.url cannot be null");
        if (webhook.getMethod() == null) throw new IllegalArgumentException("webhook.method cannot be null");
        if (!"POST".equals(webhook.getMethod()) && !"GET".equals(webhook.getMethod())) throw new IllegalArgumentException("webhook.method is not POST or GET");
        this.webhookList.add(webhook);
    }

    public Set<NoProxyWebhook> getWebhooks() {
        return this.webhookList;
    }

    public boolean shouldBeBlocked(String ip, String name) {

        NoProxyAddressInfo mNoProxyAddressInfo = this.infoMap.get(ip);
        Long mTime = this.timeMap.get(ip);
        long now = System.currentTimeMillis();
        this.checkForDiscard();

        if ((mTime != null) && ((now - mTime) < DATA_DISCARD_TIME)) {
            if (this.debug) this.info(ip + " " + mTime + " " + now + " (" + (now - mTime) + ")");
            boolean block = (mNoProxyAddressInfo != null) && mNoProxyAddressInfo.getSuggestions().isBlock();
            if (this.webhookAlways) {
                this.dispatchAsync(() -> this.dispatchWebhooks(mNoProxyAddressInfo, block, Collections.singletonMap("nick", name)));
            }
            return block;
        }

        NoProxyAddressInfo NoProxyAddressInfo;
        try {
            NoProxyAddressInfo = this.client.getInfo(ip);
        } catch (NoProxyException exception) {
            NoProxyError apiError = exception.getApiError();
            this.timeMap.put(ip, now);
            this.warning("Error communicating OK! No.Proxy API: " + exception.getMessage());
            if (this.debug) exception.printStackTrace();
            return false;
        }

        boolean block = NoProxyAddressInfo.getSuggestions().isBlock();
        this.timeMap.put(ip, now);
        this.infoMap.put(ip, NoProxyAddressInfo);
        this.dispatchAsync(() -> this.dispatchWebhooks(NoProxyAddressInfo, block, Collections.singletonMap("nick", name)));

        if (block) {
            this.info("Blocked IP address '" + ip + "' [" + NoProxyAddressInfo.getGeneral().getCountry() + ", AS" + NoProxyAddressInfo.getGeneral().getAsn() + "]");
        }

        return block;
    }

    public boolean shouldBeBlocked(String ip) {
        return this.shouldBeBlocked(ip, null);
    }

    private void checkForDiscard() {

        long now = System.currentTimeMillis();
        if (now < this.nextDiscard) {
            return;
        }

        for (Map.Entry<String, Long> entry : this.timeMap.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            if ((now - value) < DATA_DISCARD_TIME) {
                continue;
            }
            if (this.debug) this.info("discard " + key + " " + value + " " + now);
            this.timeMap.remove(key);
            this.infoMap.remove(key);
        }

        this.nextDiscard = now + DATA_DISCARD_TIME;
    }

    public void dispatchWebhooks(NoProxyAddressInfo info, boolean block, Map<String, String> additionalVariables) {
        if (this.debug) this.info("Checking for webhooks (" + this.webhookList.size() + ")");
        for (NoProxyWebhook webhook : this.webhookList) {
            if (this.debug) this.info("Analzying (block=" + block + "): " + webhook);
            if (webhook.isBlockedOnly()) {
                if (block) {
                    this.dispatchWebhook(info, webhook, additionalVariables);
                }
            } else {
                this.dispatchWebhook(info, webhook, additionalVariables);
            }
        }
    }

    public void dispatchWebhook(NoProxyAddressInfo info, NoProxyWebhook webhook, Map<String, String> additionalVariables) {
        if (this.debug) this.info("Preparing webhook for " + webhook);
        if (webhook.getUrl() == null) throw new IllegalArgumentException("webhook.url cannot be null");
        String method = (webhook.getMethod() == null) ? "GET" : webhook.getMethod();
        String url = this.replaceVariables(webhook.getUrl(), info, true, false, this.addressInfoToMap(info), additionalVariables);
        String body = (webhook.getContent() == null) ? "" : webhook.getContent();
        body = this.replaceVariables(body, info, false, true, this.addressInfoToMap(info), additionalVariables);
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

    private Map<String, String> addressInfoToMap(NoProxyAddressInfo info) {
        Map<String, String> map = new HashMap<>();
        map.put("general.ip", info.getGeneral().getIp());
        map.put("general.asn", String.valueOf(info.getGeneral().getAsn()));
        map.put("general.provider", info.getGeneral().getProvider());
        map.put("general.country", info.getGeneral().getCountry());
        map.put("risks.total", String.valueOf(info.getRisks().getTotal()));
        map.put("risks.proxy", String.valueOf(info.getRisks().isProxy()));
        map.put("risks.country", String.valueOf(info.getRisks().isCountry()));
        map.put("risks.asn", String.valueOf(info.getRisks().isAsn()));
        map.put("risks.provider", String.valueOf(info.getRisks().isProvider()));
        map.put("score.noproxy", String.valueOf(info.getScore().getNoproxy()));
        map.put("score.abuseipdb", String.valueOf(info.getScore().getAbuseipdb()));
        map.put("suggestions.verify", String.valueOf(info.getSuggestions().isVerify()));
        map.put("suggestions.block", String.valueOf(info.getSuggestions().isBlock()));
        return map;
    }

    @SafeVarargs
    private final String replaceVariables(String text, NoProxyAddressInfo info, boolean urlEncode, boolean quotesEscape, Map<String, String>... variableSets) {
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
