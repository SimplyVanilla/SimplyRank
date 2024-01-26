package net.simplyvanilla.simplyrank.database.addresswhitelist;

public interface AddressWhitelistRepository {
    void save(AddressWhitelist addressWhitelist);

    boolean existsByAddress(String address);

    void deleteByAddress(String address);
}
