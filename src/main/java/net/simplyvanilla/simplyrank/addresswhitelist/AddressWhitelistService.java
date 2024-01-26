package net.simplyvanilla.simplyrank.addresswhitelist;

import net.simplyvanilla.simplyrank.database.addresswhitelist.AddressWhitelist;
import net.simplyvanilla.simplyrank.database.addresswhitelist.AddressWhitelistRepository;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

public class AddressWhitelistService {
    private final AddressWhitelistRepository repository;

    public AddressWhitelistService(AddressWhitelistRepository repository) {
        this.repository = repository;
    }

    public void addAddress(String address, UUID invokerId) {
        this.repository.save(new AddressWhitelist(address, invokerId, LocalDateTime.now()));
    }

    public void removeAddress(String address) {
        this.repository.deleteByAddress(address);
    }

    public boolean isWhitelisted(Player player) {
        String address = player.getAddress().getAddress().getHostAddress();
        return this.repository.existsByAddress(address);
    }
}
