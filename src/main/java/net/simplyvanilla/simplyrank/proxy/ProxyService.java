package net.simplyvanilla.simplyrank.proxy;

import net.simplyvanilla.simplyrank.database.proxycache.ProxyCacheRepository;
import net.simplyvanilla.simplyrank.database.proxycache.ProxyData;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyProvider;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ProxyService {
    private final ProxyCacheRepository repository;
    private final ProxyProvider provider;

    public ProxyService(ProxyCacheRepository repository, ProxyProvider provider) {
        this.repository = repository;
        this.provider = provider;
    }

    public boolean isDenied(Player player) {
        String addressOfPlayer = player.getAddress().getAddress().getHostAddress();

        ProxyData proxyData = this.repository.findByAddress(addressOfPlayer).orElseGet(() -> fetchFromProvider(addressOfPlayer));

        // We couldn't fetch the proxy data from the provider, so we'll just assume they're not a proxy
        if (proxyData == null) {
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
        if (result == null) return null;
        var data = result.toData(addressOfPlayer);
        this.repository.insert(data);
        return data;
    }

    public void deleteExpiredEntries(int minutes) {
        this.repository.deleteExpiredEntries(minutes);
    }
}
