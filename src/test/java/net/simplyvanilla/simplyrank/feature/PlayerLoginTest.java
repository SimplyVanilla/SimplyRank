package net.simplyvanilla.simplyrank.feature;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.format.NamedTextColor;
import net.simplyvanilla.simplyrank.addresswhitelist.AddressWhitelistService;
import net.simplyvanilla.simplyrank.database.AddressWhitelistRepositoryMock;
import net.simplyvanilla.simplyrank.database.GroupRepositoryMock;
import net.simplyvanilla.simplyrank.database.PlayerDataRepositoryMock;
import net.simplyvanilla.simplyrank.database.ProxyCacheRepositoryMock;
import net.simplyvanilla.simplyrank.database.group.GroupData;
import net.simplyvanilla.simplyrank.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.database.player.PlayerData;
import net.simplyvanilla.simplyrank.database.player.PlayerDataRepository;
import net.simplyvanilla.simplyrank.database.proxycache.ProxyCacheRepository;
import net.simplyvanilla.simplyrank.listener.PlayerLoginEventListener;
import net.simplyvanilla.simplyrank.permission.GroupPermissionService;
import net.simplyvanilla.simplyrank.permission.PermissionApplyService;
import net.simplyvanilla.simplyrank.permission.PlayerDataService;
import net.simplyvanilla.simplyrank.permission.PlayerPermissionService;
import net.simplyvanilla.simplyrank.player.CustomPlayerMock;
import net.simplyvanilla.simplyrank.proxy.ProxyService;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyProvider;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyResult;
import net.simplyvanilla.simplyrank.proxy.provider.ProxyType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class PlayerLoginTest {

    private ServerMock server;
    private MockPlugin plugin;
    private GroupRepository groupRepository;
    private PlayerDataRepository playerDataRepository;
    private PlayerDataService playerDataService;
    private PlayerPermissionService playerPermissionService;
    private GroupPermissionService groupPermissionService;
    private PermissionApplyService permissionApplyService;
    private ProxyService defaultProxyService;
    private AddressWhitelistService addressWhitelistService;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.createMockPlugin();
        this.groupRepository = new GroupRepositoryMock();
        this.playerDataRepository = new PlayerDataRepositoryMock();
        this.playerDataService = new PlayerDataService(this.groupRepository, this.playerDataRepository);
        this.playerPermissionService = new PlayerPermissionService(this.plugin, this.playerDataService);
        this.groupPermissionService = new GroupPermissionService();
        this.permissionApplyService = new PermissionApplyService(this.plugin, this.playerDataService, this.playerPermissionService, this.groupPermissionService);
        this.defaultProxyService = new ProxyService(new ProxyCacheRepositoryMock(), address -> null);
        this.addressWhitelistService = new AddressWhitelistService(new AddressWhitelistRepositoryMock());
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testIfJoinedPlayerHasDefaultGroup() {
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.groupPermissionService.setPermission("admin", "test.permission", true);
        this.groupPermissionService.setPermission("default", "test.permission2", true);

        this.server.getPluginManager().registerEvents(new PlayerLoginEventListener(this.plugin, this.permissionApplyService, this.defaultProxyService, this.addressWhitelistService), this.plugin);

        PlayerMock player = new PlayerMock(this.server, "MockPlayer", UUID.randomUUID());
        this.server.addPlayer(player);


        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            try {
                this.server.getScheduler().performOneTick();
                return player.hasPermission("test.permission2");
            } catch (UnimplementedOperationException ignored) {
//            // this error comes because updateCommands is not implemented in MockBukkit
                return true;
            }
        });

        Assertions.assertFalse(player.hasPermission("test.permission"));
        Assertions.assertTrue(player.hasPermission("test.permission2"));
    }

    @Test
    void testIfJoinedPlayerHasAdminGroup() {
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.groupPermissionService.setPermission("admin", "test.permission", true);
        this.groupPermissionService.setPermission("default", "test.permission2", true);

        this.server.getPluginManager().registerEvents(new PlayerLoginEventListener(this.plugin, this.permissionApplyService, this.defaultProxyService, this.addressWhitelistService), this.plugin);

        UUID playerId = UUID.randomUUID();
        this.playerDataRepository.save(playerId, new PlayerData(List.of("admin")));
        PlayerMock player = new PlayerMock(this.server, "MockPlayer", playerId);
        this.server.addPlayer(player);


        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            try {
                this.server.getScheduler().performOneTick();
                return player.hasPermission("test.permission");
            } catch (UnimplementedOperationException ignored) {
//            // this error comes because updateCommands is not implemented in MockBukkit
                return true;
            }
        });

        Assertions.assertTrue(player.hasPermission("test.permission"));
        Assertions.assertFalse(player.hasPermission("test.permission2"));
    }

    @Test
    void testIfJoinedPlayerIsDeniedIfVpn() {
        ProxyService proxyService = new ProxyService(new ProxyCacheRepositoryMock(), address -> new ProxyResult(ProxyType.VPN, false));
        this.server.getPluginManager().registerEvents(new PlayerLoginEventListener(this.plugin, this.permissionApplyService, proxyService, this.addressWhitelistService), this.plugin);

        PlayerMock player = new CustomPlayerMock(this.server, "MockPlayer", UUID.randomUUID());
        player.setAddress(new InetSocketAddress("1.1.1.1", 1337));
        this.server.addPlayer(player);
        Assertions.assertTrue(proxyService.isDenied(player));

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            try {
                this.server.getScheduler().performOneTick();
                return !player.isOnline();
            } catch (UnimplementedOperationException ignored) {
//            // this error comes because updateCommands is not implemented in MockBukkit
                return true;
            }
        });
    }

    @Test
    void testIfPlayerJoinsWithVpnAndIsWhitelisted() {
        ProxyService proxyService = new ProxyService(new ProxyCacheRepositoryMock(), address -> new ProxyResult(ProxyType.VPN, false));
        this.server.getPluginManager().registerEvents(new PlayerLoginEventListener(this.plugin, this.permissionApplyService, proxyService, this.addressWhitelistService), this.plugin);

        PlayerMock player = new CustomPlayerMock(this.server, "MockPlayer", UUID.randomUUID());
        player.setAddress(new InetSocketAddress("1.1.1.1", 1337));
        this.addressWhitelistService.addAddress("1.1.1.1", player.getUniqueId());
        this.server.addPlayer(player);
        Assertions.assertTrue(proxyService.isDenied(player));


        Assertions.assertTrue(this.addressWhitelistService.isWhitelisted(player));

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            try {
                this.server.getScheduler().performOneTick();
                return player.isOnline();
            } catch (UnimplementedOperationException ignored) {
//            // this error comes because updateCommands is not implemented in MockBukkit
                return true;
            }
        });
    }

    @Test
    void testIfProxyCacheIsWorking() {
        ProxyCacheRepository proxyCacheRepository = new ProxyCacheRepositoryMock();
        ProxyProvider proxyProvider = address -> {
            if (address.equals("1.1.1.1")) {
                return new ProxyResult(ProxyType.VPN, true);
            } else {
                return new ProxyResult(ProxyType.RESIDENTIAL, false);
            }
        };
        ProxyService proxyService = new ProxyService(proxyCacheRepository, proxyProvider);
//        this.server.getPluginManager().registerEvents(new PlayerLoginEventListener(this.plugin, this.permissionApplyService, proxyService, this.addressWhitelistService), this.plugin);

        PlayerMock player = new CustomPlayerMock(this.server, "MockPlayer1", UUID.randomUUID());
        player.setAddress(new InetSocketAddress("1.1.1.1", 1337));
        this.server.addPlayer(player);
        Assertions.assertTrue(proxyCacheRepository.findByAddress("1.1.1.1").isEmpty());
        Assertions.assertEquals(ProxyType.VPN, Objects.requireNonNull(proxyProvider.fetch("1.1.1.1")).type());
        Assertions.assertEquals(ProxyType.RESIDENTIAL, Objects.requireNonNull(proxyProvider.fetch("8.8.8.8")).type());
        Assertions.assertTrue(proxyService.isDenied(player));
        Assertions.assertTrue(proxyCacheRepository.findByAddress("1.1.1.1").isPresent());
    }
}
