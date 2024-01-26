package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.simplyvanilla.simplyrank.addresswhitelist.AddressWhitelistService;
import net.simplyvanilla.simplyrank.database.AddressWhitelistRepositoryMock;
import net.simplyvanilla.simplyrank.database.addresswhitelist.AddressWhitelistRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.UUID;

public class AddressWhitelistServiceTest {
    private ServerMock server;
    private AddressWhitelistRepository addressWhitelistRepository;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.addressWhitelistRepository = new AddressWhitelistRepositoryMock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testAddAddress() {
        AddressWhitelistService service = new AddressWhitelistService(this.addressWhitelistRepository);

        service.addAddress("1.1.1.1", UUID.randomUUID());
        Assertions.assertTrue(this.addressWhitelistRepository.existsByAddress("1.1.1.1"));
    }

    @Test
    void testRemoveAddress() {
        AddressWhitelistService service = new AddressWhitelistService(this.addressWhitelistRepository);

        service.addAddress("1.1.1.1", UUID.randomUUID());
        Assertions.assertTrue(this.addressWhitelistRepository.existsByAddress("1.1.1.1"));
        service.removeAddress("1.1.1.1");
        Assertions.assertFalse(this.addressWhitelistRepository.existsByAddress("1.1.1.1"));
    }

    @Test
    void testIfPlayerIsWhitelisted() {
        AddressWhitelistService service = new AddressWhitelistService(this.addressWhitelistRepository);

        PlayerMock player = this.server.addPlayer();
        player.setAddress(new InetSocketAddress("1.1.1.1", 1337));
        Assertions.assertFalse(service.isWhitelisted(player));
        service.addAddress("1.1.1.1", player.getUniqueId());
        Assertions.assertTrue(service.isWhitelisted(player));
    }
}
