package net.simplyvanilla.simplyrank.database;

import net.simplyvanilla.simplyrank.database.addresswhitelist.AddressWhitelist;
import net.simplyvanilla.simplyrank.database.addresswhitelist.AddressWhitelistRepository;

import java.util.HashMap;
import java.util.Map;

public class AddressWhitelistRepositoryMock implements AddressWhitelistRepository {
    private final Map<String, AddressWhitelist> values = new HashMap<>();

    @Override
    public void save(AddressWhitelist addressWhitelist) {
        values.put(addressWhitelist.getAddress(), addressWhitelist);
    }

    @Override
    public boolean existsByAddress(String address) {
        return values.containsKey(address);
    }

    @Override
    public void deleteByAddress(String address) {
        values.remove(address);
    }
}
