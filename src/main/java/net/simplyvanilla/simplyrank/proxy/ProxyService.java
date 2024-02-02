package net.simplyvanilla.simplyrank.proxy;

import net.simplyvanilla.simplyrank.database.proxycache.ProxyCacheRepository;
import net.simplyvanilla.simplyrank.database.proxycache.ProxyData;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyProvider;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyService.class);
    private final ProxyCacheRepository repository;
    private final ProxyProvider provider;

    public ProxyService(ProxyCacheRepository repository, ProxyProvider provider) {
        this.repository = repository;
        this.provider = provider;
    }

    public boolean isDenied(Player player) {
        String addressOfPlayer = player.getAddress().getAddress().getHostAddress();

        ProxyData proxyData = this.repository.findByAddress(addressOfPlayer).orElseGet(() -> this.fetchFromProvider(addressOfPlayer));
        LOGGER.info("Proxy data for player {} ({}) is {}", player.getName(), player.getUniqueId(), proxyData);
        // We couldn't fetch the proxy data from the provider, so we'll just assume they're not a proxy
        if (proxyData == null) {
            LOGGER.warn("Could not fetch proxy data for player {} ({})", player.getName(), player.getUniqueId());
            return false;
        }

        return this.isVpn(proxyData);
    }

    public boolean isVpn(ProxyData proxyData) {
        return proxyData.type() == ProxyType.VPN || proxyData.proxy();
    }

    @Nullable
    private ProxyData fetchFromProvider(String addressOfPlayer) {
        var result = this.provider.fetch(addressOfPlayer);
        // We couldn't fetch the proxy data from the provider
        if (result == null) {
            return null;
        }
        var data = result.toData(addressOfPlayer);
        this.repository.insert(data);
        return data;
    }

    public void deleteExpiredEntries(int minutes) {
        this.repository.deleteExpiredEntries(minutes);
    }
}
