package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.format.NamedTextColor;
import net.simplyvanilla.simplyrank.database.GroupRepositoryMock;
import net.simplyvanilla.simplyrank.database.PlayerDataRepositoryMock;
import net.simplyvanilla.simplyrank.database.group.GroupData;
import net.simplyvanilla.simplyrank.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.database.player.PlayerData;
import net.simplyvanilla.simplyrank.database.player.PlayerDataRepository;
import net.simplyvanilla.simplyrank.permission.GroupPermissionService;
import net.simplyvanilla.simplyrank.permission.PermissionApplyService;
import net.simplyvanilla.simplyrank.permission.PlayerDataService;
import net.simplyvanilla.simplyrank.permission.PlayerPermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
        this.playerDataService = new PlayerDataService(this.groupRepository, this.playerDataRepository);
        this.playerPermissionService = new PlayerPermissionService(this.plugin, this.playerDataService);
        this.groupPermissionService = new GroupPermissionService();
        this.permissionApplyService = new PermissionApplyService(this.plugin, this.playerDataService, this.playerPermissionService, this.groupPermissionService);
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
}
