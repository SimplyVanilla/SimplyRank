package net.simplyvanilla.simplyrank.proxy.provider;

import net.simplyvanilla.simplyrank.database.proxycache.ProxyData;

import java.time.LocalDateTime;

public record ProxyResult(ProxyType type, boolean proxy) {
    public ProxyData toData(String address) {
        return new ProxyData(address, this.type, this.proxy, LocalDateTime.now(), false);
    }
}
