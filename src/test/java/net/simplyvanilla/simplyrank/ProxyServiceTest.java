package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.simplyvanilla.simplyrank.database.ProxyCacheRepositoryMock;
import net.simplyvanilla.simplyrank.database.proxycache.ProxyCacheRepository;
import net.simplyvanilla.simplyrank.database.proxycache.ProxyData;
import net.simplyvanilla.simplyrank.proxy.ProxyService;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyResult;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

class ProxyServiceTest {

    private ServerMock server;
    private ProxyCacheRepository proxyCacheRepository;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.proxyCacheRepository = new ProxyCacheRepositoryMock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testIfVpnProxyDataIsVpn() {
        ProxyService service = new ProxyService(this.proxyCacheRepository, address -> null);

        Assertions.assertTrue(service.isVpn(new ProxyData("", ProxyType.VPN, true, LocalDateTime.now())));
    }

    @Test
    void testDeleteExpiredEntries() {
        ProxyService service = new ProxyService(this.proxyCacheRepository, address -> null);

        this.proxyCacheRepository.insert(new ProxyData("1.1.1.1", ProxyType.HTTP, false, LocalDateTime.now().minusMinutes(2)));
        this.proxyCacheRepository.insert(new ProxyData("1.1.1.2", ProxyType.HTTP, false, LocalDateTime.now()));
        Assertions.assertTrue(this.proxyCacheRepository.findByAddress("1.1.1.1").isPresent());
        service.deleteExpiredEntries(1);
        Assertions.assertFalse(this.proxyCacheRepository.findByAddress("1.1.1.1").isPresent());
        Assertions.assertTrue(this.proxyCacheRepository.findByAddress("1.1.1.2").isPresent());
    }

    @Test
    void testIfPlayerIsDeniedIfVpn() {
        ProxyService service = new ProxyService(this.proxyCacheRepository, address -> new ProxyResult(ProxyType.VPN, false));

        Assertions.assertTrue(service.isDenied(this.server.addPlayer()));
    }

    @Test
    void testIfPlayerIsDeniedIfProxy() {
        ProxyService service = new ProxyService(this.proxyCacheRepository, address -> new ProxyResult(ProxyType.HTTP, true));

        Assertions.assertTrue(service.isDenied(this.server.addPlayer()));
    }

    @Test
    void testIfPlayerIsNotDeniedIfNotProxy() {
        ProxyService service = new ProxyService(this.proxyCacheRepository, address -> new ProxyResult(ProxyType.RESIDENTIAL, false));

        Assertions.assertFalse(service.isDenied(this.server.addPlayer()));
    }

    @Test
    void testIfProxyResultIsNull() {
        ProxyService service = new ProxyService(this.proxyCacheRepository, address -> null);

        Assertions.assertFalse(service.isDenied(this.server.addPlayer()));
    }


}
