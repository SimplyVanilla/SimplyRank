package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.format.NamedTextColor;
import net.simplyvanilla.simplyrank.data.GroupPermissionService;
import net.simplyvanilla.simplyrank.data.PermissionApplyService;
import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.PlayerPermissionService;
import net.simplyvanilla.simplyrank.data.database.group.GroupData;
import net.simplyvanilla.simplyrank.data.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import net.simplyvanilla.simplyrank.data.database.player.PlayerDataRepository;
import net.simplyvanilla.simplyrank.database.GroupRepositoryMock;
import net.simplyvanilla.simplyrank.database.PlayerDataRepositoryMock;
import net.simplyvanilla.simplyrank.listener.PlayerLoginEventListener;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class PermissionApplyServiceTest {
    private ServerMock server;
    private MockPlugin plugin;
    private GroupRepository groupRepository;
    private PlayerDataRepository playerDataRepository;
    private PlayerDataService playerDataService;
    private PlayerPermissionService playerPermissionService;
    private GroupPermissionService groupPermissionService;
    private PermissionApplyService permissionApplyService;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.createMockPlugin();
        this.groupRepository = new GroupRepositoryMock();
        this.playerDataRepository = new PlayerDataRepositoryMock();
        this.playerDataService = new PlayerDataService(this.plugin, this.groupRepository, this.playerDataRepository);
        this.playerPermissionService = new PlayerPermissionService(this.plugin, this.playerDataService);
        this.groupPermissionService = new GroupPermissionService();
        this.permissionApplyService = new PermissionApplyService(this.playerDataService, this.playerPermissionService, this.groupPermissionService);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testApplyPermissionWithAdminGroup() {
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.groupPermissionService.setPermission("admin", "test.permission", true);
        this.groupPermissionService.setPermission("default", "test.permission2", true);

        PlayerMock player = this.server.addPlayer();
        UUID uniqueId = player.getUniqueId();
        this.playerDataRepository.save(uniqueId, new PlayerData(List.of("admin")));

        PlayerData playerData = this.playerDataService.loadPlayerData(uniqueId);
        Assertions.assertEquals("admin", playerData.getPrimaryGroup());

        try {
            this.permissionApplyService.applyPermission(player, playerData);
        } catch (UnimplementedOperationException ignored) {
//            // this error comes because updateCommands is not implemented in MockBukkit
        }

        Assertions.assertTrue(player.hasPermission("test.permission"));
        Assertions.assertFalse(player.hasPermission("test.permission2"));
    }

    @Test
    void testApplyPermissionWithNewPlayer() {
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.groupPermissionService.setPermission("admin", "test.permission", true);
        this.groupPermissionService.setPermission("default", "test.permission2", true);

        PlayerMock player = this.server.addPlayer();
        UUID uniqueId = player.getUniqueId();

        PlayerData playerData = this.playerDataService.loadPlayerData(uniqueId);
        Assertions.assertEquals("default", playerData.getPrimaryGroup());

        try {
            this.permissionApplyService.applyPermission(player, playerData);
        } catch (UnimplementedOperationException ignored) {
//            // this error comes because updateCommands is not implemented in MockBukkit
        }

        Assertions.assertFalse(player.hasPermission("test.permission"));
        Assertions.assertTrue(player.hasPermission("test.permission2"));
    }

    @Test
    void testApplyWithNewPlayer() {
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.groupPermissionService.setPermission("admin", "test.permission", true);
        this.groupPermissionService.setPermission("default", "test.permission2", true);

        PlayerMock player = this.server.addPlayer();

        this.permissionApplyService.apply(player);

        Assertions.assertFalse(player.hasPermission("test.permission"));
        Assertions.assertTrue(player.hasPermission("test.permission2"));
    }

    @Test
    void testIfJoinedPlayerHasDefaultGroup() {
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.groupPermissionService.setPermission("admin", "test.permission", true);
        this.groupPermissionService.setPermission("default", "test.permission2", true);

        this.server.getPluginManager().registerEvents(new PlayerLoginEventListener(this.plugin, this.permissionApplyService), this.plugin);

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

        this.server.getPluginManager().registerEvents(new PlayerLoginEventListener(this.plugin, this.permissionApplyService), this.plugin);

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
}
