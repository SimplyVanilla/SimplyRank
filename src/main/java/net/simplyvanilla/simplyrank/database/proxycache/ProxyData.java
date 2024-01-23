package net.simplyvanilla.simplyrank.database.proxycache;

import net.simplyvanilla.simplyrank.proxy.provider.ProxyType;

import java.time.LocalDateTime;

public record ProxyData(String address, ProxyType type, boolean proxy, LocalDateTime fetchedAt) {
}
