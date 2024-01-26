package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.simplyvanilla.simplyrank.permission.PlayerDataService;
import net.simplyvanilla.simplyrank.permission.PlayerPermissionService;
import net.simplyvanilla.simplyrank.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.database.player.PlayerDataRepository;
import net.simplyvanilla.simplyrank.database.GroupRepositoryMock;
import net.simplyvanilla.simplyrank.database.PlayerDataRepositoryMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerPermissionServiceTest {
    private ServerMock server;
    private MockPlugin plugin;
    private GroupRepository groupRepository;
    private PlayerDataRepository playerDataRepository;
    private PlayerDataService playerDataService;
    private PlayerPermissionService service;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.createMockPlugin();
        this.groupRepository = new GroupRepositoryMock();
        this.playerDataRepository = new PlayerDataRepositoryMock();
        this.playerDataService = new PlayerDataService(this.groupRepository, this.playerDataRepository);
        this.service = new PlayerPermissionService(this.plugin, this.playerDataService);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testSetPermission() {
        PlayerMock player = this.server.addPlayer();

        this.service.setPermission(player, "test.permission", true);

        Assertions.assertTrue(player.hasPermission("test.permission"));
    }
    @Test
    void testClear() {
        PlayerMock player = this.server.addPlayer();

        this.service.setPermission(player, "test.permission", true);
        this.service.clear(player);

        Assertions.assertFalse(player.hasPermission("test.permission"));
    }
}
