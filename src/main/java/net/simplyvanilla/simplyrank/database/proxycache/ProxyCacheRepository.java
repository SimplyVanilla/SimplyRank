package net.simplyvanilla.simplyrank.database.proxycache;

import java.util.Optional;

public interface ProxyCacheRepository {
    Optional<ProxyData> findByAddress(String address);

    void insert(ProxyData proxyData);

    void deleteExpiredEntries(int minutes);

}
