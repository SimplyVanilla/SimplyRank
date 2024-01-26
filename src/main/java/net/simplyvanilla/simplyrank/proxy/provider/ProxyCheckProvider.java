package net.simplyvanilla.simplyrank.proxy.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProxyCheckProvider implements ProxyProvider {
    private static final String API_URL = "https://proxycheck.io/v2/%s&vpn=1";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyCheckProvider.class);

    private final HttpClient client;

    public ProxyCheckProvider() {
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public ProxyResult fetch(String address) {
        try {
            String response = this.client.send(HttpRequest.newBuilder()
                .uri(URI.create(String.format(API_URL, address)))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString()
            ).body();

            return this.parseResponse(response, address);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Failed to fetch proxy type for address {}", address, e);
        }

        return null;
    }

    public ProxyResult parseResponse(String content, String address) {
        JsonObject object = JsonParser.parseString(content).getAsJsonObject();

        if (object.get("status").getAsString().equals("ok")) {
            return null;
        }

        if (!object.has(address)) {
            return null;
        }

        JsonObject addressData = object.getAsJsonObject(address);

        return new ProxyResult(
            ProxyType.parse(addressData.get("type").getAsString()),
            addressData.get("proxy").getAsString().equals("yes")
        );
    }
}
