package net.simplyvanilla.simplyrank.database;

import net.simplyvanilla.simplyrank.database.proxycache.ProxyCacheRepository;
import net.simplyvanilla.simplyrank.database.proxycache.ProxyData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProxyCacheRepositoryMock implements ProxyCacheRepository {
    private final List<ProxyData> values = new ArrayList<>();

    @Override
    public Optional<ProxyData> findByAddress(String address) {
        return this.values.stream().filter(proxyData -> proxyData.address().equals(address))
            .findFirst();
    }

    @Override
    public void insert(ProxyData proxyData) {
        this.values.add(proxyData);
    }

    @Override
    public void deleteExpiredEntries(int minutes) {
        this.values.removeIf(proxyData -> proxyData.fetchedAt().isBefore(LocalDateTime.now().minus(minutes, ChronoUnit.MINUTES)));
    }
}
