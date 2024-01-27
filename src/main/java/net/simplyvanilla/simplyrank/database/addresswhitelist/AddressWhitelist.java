package net.simplyvanilla.simplyrank.database.addresswhitelist;

import java.time.LocalDateTime;
import java.util.UUID;

public class AddressWhitelist {
    private final String address;
    private final UUID invokerId;
    private final LocalDateTime createdAt;

    public AddressWhitelist(String address, UUID invokerId, LocalDateTime createdAt) {
        this.address = address;
        this.invokerId = invokerId;
        this.createdAt = createdAt;
    }

    public String getAddress() {
        return this.address;
    }

    public UUID getInvokerId() {
        return this.invokerId;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
