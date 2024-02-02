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
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyCheckProvider.class);

    private final String apiUrl;
    private final HttpClient client;

    public ProxyCheckProvider(String apiUrl)
    {
        this.apiUrl = apiUrl;
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public ProxyResult fetch(String address) {
        try {
            String response = this.client.send(HttpRequest.newBuilder()
                .uri(URI.create(String.format(this.apiUrl, address)))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString()
            ).body();

            LOGGER.debug("Response from proxy check provider: {}", response);
            return this.parseResponse(response, address);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Failed to fetch proxy type for address {}", address, e);
        }

        return null;
    }

    public ProxyResult parseResponse(String content, String address) {
        JsonObject object = JsonParser.parseString(content).getAsJsonObject();

        // We check if the response contains a address object instead of status
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
